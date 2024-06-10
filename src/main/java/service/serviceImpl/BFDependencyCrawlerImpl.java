package service.serviceImpl;

import data.Component;
import data.Dependency;
import data.Property;
import logger.Logger;
import repository.ComponentRepository;
import service.BFDependencyCrawler;

import java.text.DecimalFormat;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {
    private static final Logger logger = Logger.of("DependencyCrawler");

    public void crawl(Component parentComponent) {

        var time = System.currentTimeMillis();
        logger.info("Crawling dependencies of " + parentComponent.getQualifiedName() + "...");
        int numberOfComponents = crawlMulti(parentComponent);


        double timeTaken = (System.currentTimeMillis() - time) / 1000.0;
        var format = new DecimalFormat("0.##");
        logger.success("Crawling finished in " + timeTaken + "s. (" + format.format(timeTaken / numberOfComponents) + "s per component)");
    }

    public int crawlMulti(Component parentComponent) {
        parentComponent.loadComponent();
        var repository = parentComponent.getRepository();

        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);
        var queue = new ConcurrentLinkedDeque<>(parentComponent.getDependenciesFiltered());
        AtomicInteger activeTasks = new AtomicInteger(0);

        while (!queue.isEmpty() || activeTasks.get() > 0) {
            Dependency dependency = queue.poll();
            if (dependency != null) {
                activeTasks.incrementAndGet();
                completionService.submit(() -> {
                    try {
                        //get the version of the dependency
                        if (!dependency.hasVersion()) {
                            repository.getVersionResolver().resolveVersion(dependency);
                        }

                        //get the component of the dependency
                        var component = dependency.getComponent();

                        // process the component
                        if (component != null)
                            if (!component.isLoaded()) {
                                component.loadComponent();

                                if (component.isLoaded()) {
                                    queue.addAll(component.getDependenciesFiltered());

                                    component.getDependencies().forEach(c -> {
                                        if (!c.isNotOptional()) {
                                            logger.info("Dependency " + c + " is not resolved because it is optional.");
                                        } else if (!c.shouldResolveByScope()) {
                                            logger.info("Dependency " + c + " is not resolved because it is of scope \"" + c.getScope() + "\".");
                                        }
                                    });

                                    loadCount.getAndIncrement();
                                } else {
                                    failCount.getAndIncrement();
                                }
                            }

                    } catch (Exception e) {
                        logger.error("Failed to resolve dependency " + dependency + ": " + e.getMessage());
                        failCount.getAndIncrement();
                    } finally {
                        activeTasks.decrementAndGet();
                    }
                    return null;
                });
            } else {
                try {
                    Future<Void> future = completionService.poll(100, TimeUnit.MILLISECONDS);
                    if (future != null) {
                        future.get(); // ensure exceptions are propagated
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted while waiting for tasks to complete." + e);
                    break;
                } catch (Exception e) {
                    logger.error("Error occurred during task execution." + e);
                }
            }
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.error("Failed to resolve all components within the timeout period.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while waiting for executor service to terminate." + e);
            executorService.shutdownNow();
        }

        logger.info("Resolved " + loadCount.get() + " components.");
        if (failCount.get() > 0) {
            logger.error("Failed to resolve " + failCount.get() + " components.");
        } else {
            logger.info("All components resolved successfully.");
        }

        logger.info("Applying overwritten versions...");
        updateDependenciesToNewestVersion(parentComponent);

        return loadCount.get() + failCount.get();
    }

    public void updateDependenciesToNewestVersion(Component rootComponent) {
        for (var dependency : rootComponent.getDependenciesFlatFiltered()) {
            var dependencyComponent = dependency.getComponent();
            if (dependencyComponent == null) continue;
            var newestComponent = ComponentRepository.getLoadedComponents(dependencyComponent.getGroup(), dependencyComponent.getName()).last();
            if (newestComponent.getVersion().compareTo(dependencyComponent.getVersion()) > 0) {
                //check if we have a version in the tree parent
                var treeParent = dependency.getTreeParent();
                if (treeParent != null) {
                    if (treeParent.getDependenciesFiltered().stream().anyMatch(d -> d.getComponent() != null && d.getComponent().getGroup().equals(dependencyComponent.getGroup()) && d.getComponent().getName().equals(dependencyComponent.getName()))) {
                        treeParent.removeDependency(dependency);
                    }
                }

                dependency.setComponent(newestComponent);
                dependency.setVersion(newestComponent.getVersion());

                newestComponent.setData("addProperty", Property.of("overwritesDependencyVersion", dependencyComponent.getQualifiedName()));
            }
        }
    }
}
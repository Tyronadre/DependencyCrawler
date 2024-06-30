package service.serviceImpl;

import data.Component;
import data.Dependency;
import data.Property;
import data.ReadComponent;
import data.internalData.MavenComponent;
import logger.Logger;
import repository.ComponentRepository;
import service.BFDependencyCrawler;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Objects;
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

    public void crawl(Component parentComponent, Boolean updateDependenciesToNewestVersion) {

        var time = System.currentTimeMillis();
        logger.info("Crawling dependencies of " + parentComponent.getQualifiedName() + "...");
        crawlMulti(parentComponent, updateDependenciesToNewestVersion);

        var loadedComponents = ComponentRepository.getAllRepositories().stream().mapToInt(it -> it.getLoadedComponents().size()).sum();

        double timeTaken = (System.currentTimeMillis() - time) / 1000.0;
        var format = new DecimalFormat("0.##");

        logger.success("Crawling finished in " + timeTaken + "s. Loaded " + loadedComponents + " Components. (" + format.format(timeTaken / loadedComponents) + "s per component)");
    }

    public void crawlMulti(Component parentComponent, Boolean updateDependenciesToNewestVersion) {
        parentComponent.loadComponent();
        var repository = parentComponent.getRepository();

        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
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

                                    loadCount.incrementAndGet();
                                } else {
                                    failCount.incrementAndGet();
                                }
                            }

                    }
                    catch (Exception e) {
                        logger.error("Failed to resolve dependency " + dependency, e);
                        failCount.incrementAndGet();
                    }
                    finally {
                        activeTasks.decrementAndGet();
                    }
                    return null;
                });
            } else {
                try {
                    Future<Void> future = completionService.poll(1, TimeUnit.MINUTES);
                    if (future != null) {
                        future.get(); // ensure exceptions are propagated
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted while waiting for tasks to complete." + e);
                    break;
                }
                catch (Exception e) {
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
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while waiting for executor service to terminate." + e);
            executorService.shutdownNow();
        }

//        logger.success("Resolved " + loadCount.get() + " components.");
//        if (failCount.get() > 0) {
//            logger.error("Failed to resolve " + failCount.get() + " components.");
//        }
//        else {
//            logger.success("All components resolved successfully.");
//        }

        if (updateDependenciesToNewestVersion) {
            logger.info("Applying overwritten versions...");
            updateDependenciesToNewestVersion(parentComponent);
        }
    }

    public void updateDependenciesToNewestVersion(Component rootComponent) {
        var queue = new ArrayDeque<>(rootComponent.getDependenciesFiltered());
        while (!queue.isEmpty()) {
            var dependency = queue.poll();
            var dependencyComponent = dependency.getComponent();
            if (dependencyComponent == null) continue;
            if (!(dependencyComponent instanceof MavenComponent) && !(dependency.getTreeParent() == rootComponent) && !(dependencyComponent instanceof ReadComponent readComponent && readComponent.getActualComponent() instanceof MavenComponent)) {
                logger.info("skipping component " + dependencyComponent + " and all its dependants " + dependencyComponent.getDependencyComponentsFlatFiltered() + " since its not a maven component and not on top level.");
                continue;
            }
            if (dependencyComponent.getRepository().getLoadedComponents(dependencyComponent.getGroup(), dependencyComponent.getArtifactId()).isEmpty())
                continue;
            var newestComponent = dependencyComponent.getRepository().getLoadedComponents(dependencyComponent.getGroup(), dependencyComponent.getArtifactId()).getLast();
            if (newestComponent.getVersion().compareTo(dependencyComponent.getVersion()) > 0) {
                //check if we have a version in the tree parent
                var treeParent = dependency.getTreeParent();
                if (treeParent != null) {
                    if (treeParent.getDependenciesFiltered().stream().anyMatch(d -> d.getComponent() != null && Objects.equals(d.getComponent().getGroup(), dependencyComponent.getGroup()) && Objects.equals(d.getComponent().getArtifactId(), dependencyComponent.getArtifactId()))) {
                        treeParent.removeDependency(dependency);
                    }
                }

                dependency.setComponent(newestComponent);
                dependency.setVersion(newestComponent.getVersion());

                newestComponent.setData("addProperty", Property.of("overwritesDependencyVersion", dependencyComponent.getQualifiedName()));
            }

            queue.addAll(dependencyComponent.getDependenciesFiltered());
        }
    }
}
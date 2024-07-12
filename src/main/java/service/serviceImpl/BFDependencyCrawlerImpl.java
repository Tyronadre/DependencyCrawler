package service.serviceImpl;

import data.Component;
import data.Dependency;
import data.Property;
import data.ReadComponent;
import data.internalData.MavenComponent;
import logger.Logger;
import repository.ComponentRepository;
import service.BFDependencyCrawler;
import settings.Settings;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {
    private static final Logger logger = Logger.of("DependencyCrawler");
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();


    @Override
    public void crawl(Component parentComponent, boolean updateDependenciesToNewestVersion) {

        var time = System.currentTimeMillis();
        logger.info("Crawling dependencies of " + parentComponent.getQualifiedName() + "...");
        crawlMulti(parentComponent);

        if (updateDependenciesToNewestVersion) {
            logger.info("Applying overwritten versions...");
            updateDependenciesToNewestVersion(parentComponent);
        } else {
            logger.info("Skipping applying overwritten versions.");
        }

        var loadedComponents = ComponentRepository.getAllRepositories().stream().mapToInt(it -> it.getLoadedComponents().size()).sum();

        double timeTaken = (System.currentTimeMillis() - time) / 1000.0;
        var format = new DecimalFormat("0.##");

        logger.success("Crawling finished in " + timeTaken + "s. Loaded " + loadedComponents + " Components. (" + format.format(timeTaken / loadedComponents) + "s per component)");
    }

    public void crawlMulti(Component parentComponent) {
        parentComponent.loadComponent();
        var repository = parentComponent.getRepository();

        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(Settings.crawlThreads);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);
        var queue = new ConcurrentLinkedDeque<>(parentComponent.getDependenciesFiltered());
        AtomicInteger activeTasks = new AtomicInteger(0);

        while (!queue.isEmpty() || activeTasks.get() > 0) {
            Dependency dependency = queue.poll();
            if (dependency != null) {
                activeTasks.incrementAndGet();
                completionService.submit(() -> crawler(dependency, repository, queue, loadCount, failCount, activeTasks));
            } else {
                try {
                    Future<Void> future = completionService.poll(1, TimeUnit.MINUTES);
                    if (future != null) {
                        future.get();
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
    }

    /**
     * Crawls the dependencies of a component. Helper for the crawl method.
     * @param dependency the dependency to resolve
     * @param repository the repository to use
     * @param queue the queue of dependencies to resolve
     * @param loadCount the count of loaded components
     * @param failCount the count of failed components
     * @param activeTasks the count of active tasks
     * @return null
     */
    private static Void crawler(Dependency dependency, ComponentRepository repository, ConcurrentLinkedDeque<Dependency> queue, AtomicInteger loadCount, AtomicInteger failCount, AtomicInteger activeTasks) {
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
                                if (Settings.crawlOptional || Settings.crawlEverything)
                                    logger.info("Dependency " + c + " is optional.");
                                else
                                    logger.info("Dependency " + c + " is not resolved because it is optional.");
                            } else if (!c.shouldResolveByScope()) {
                                if (Settings.crawlEverything)
                                    logger.info("Dependency " + c + " is of scope \"" + c.getScope() + "\". Might cannot be resolved.");
                                else
                                    logger.info("Dependency " + c + " is not resolved because it is of scope \"" + c.getScope() + "\".");
                            }
                        });

                        loadCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                }

        } catch (Exception e) {
            logger.error("Failed to resolve dependency " + dependency, e);
            failCount.incrementAndGet();
        } finally {
            activeTasks.decrementAndGet();
        }
        return null;
    }

    public void updateDependenciesToNewestVersion(Component rootComponent) {
        ConcurrentLinkedQueue<Dependency> queue = new ConcurrentLinkedQueue<>(rootComponent.getDependenciesFiltered());
        var dependenciesDone = Collections.synchronizedSet(new HashSet<>());
        Set<Future<?>> futures = new HashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Settings.crawlThreads);

        while (!queue.isEmpty()) {
            var dependency = queue.poll();
            if (dependency == null) continue;

            Future<?> future = executorService.submit(() -> {
                var dependencyComponent = dependency.getComponent();
                if (dependencyComponent == null) return;

                if (!(dependencyComponent instanceof MavenComponent) && dependency.getTreeParent() != rootComponent && !(dependencyComponent instanceof ReadComponent readComponent && readComponent.getActualComponent() instanceof MavenComponent)) {
                    logger.info("skipping component " + dependencyComponent + " and all its dependants " + dependencyComponent.getDependencyComponentsFlatFiltered() + " since its not a maven component and not on top level.");
                    return;
                }

                List<Component> loadedComponents = dependencyComponent.getRepository().getLoadedComponents(dependencyComponent.getGroup(), dependencyComponent.getArtifactId());
                if (loadedComponents.isEmpty()) return;

                var newestComponent = loadedComponents.get(loadedComponents.size() - 1);
                if (newestComponent.getVersion().compareTo(dependencyComponent.getVersion()) > 0) {
                    var treeParent = dependency.getTreeParent();
                    if (treeParent != null) {
                        synchronized (treeParent) {
                            if (treeParent.getDependenciesFiltered().stream().anyMatch(d -> d.getComponent() != null && Objects.equals(d.getComponent().getGroup(), dependencyComponent.getGroup()) && Objects.equals(d.getComponent().getArtifactId(), dependencyComponent.getArtifactId()))) {
                                treeParent.removeDependency(dependency);
                            }
                        }
                    }

                    dependency.setComponent(newestComponent);
                    dependency.setVersion(newestComponent.getVersion());

                    newestComponent.setData("addProperty", Property.of("overwritesDependencyVersion", dependencyComponent.getQualifiedName()));
                }

                synchronized (dependenciesDone) {
                    dependenciesDone.addAll(dependencyComponent.getDependenciesFiltered().stream().map(Object::hashCode).toList());
                    queue.addAll(dependencyComponent.getDependenciesFiltered().stream().filter(d -> !dependenciesDone.contains(d.hashCode())).toList());
                }
            });

            futures.add(future);
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error updating dependencies: " + e.getMessage());
            }
        }

        // Shut down the executor service
        executorService.shutdown();
    }
}
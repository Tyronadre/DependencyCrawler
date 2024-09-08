package service.serviceImpl;

import data.Component;
import data.Dependency;
import data.Property;
import data.internalData.MavenComponent;
import exceptions.VersionResolveException;
import logger.Logger;
import repository.ComponentRepository;
import service.BFDependencyCrawler;
import settings.Settings;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {
    private static final Logger logger = Logger.of("DependencyCrawler");
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();


    @Override
    public void crawl(Component parentComponent, boolean updateDependenciesToNewestVersion) {

        var time = System.currentTimeMillis();
        logger.info("Crawling dependencies of " + parentComponent.getQualifiedName() + "...");
        if (Settings.crawlSingle) crawlSingle(parentComponent);
        else crawlMulti2(parentComponent);

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

    private void crawlSingle(Component component) {
        component.loadComponent();
        var queue = new ArrayDeque<>(component.getDependenciesFiltered());

        while (!queue.isEmpty()) {
            Dependency dependency = queue.poll();

            //resolve version if possible
            if (!dependency.hasVersion()) {
                var repositoryOfDependency = dependency.getType().getRepository();
                if (repositoryOfDependency != null) {
                    try {
                        repositoryOfDependency.getVersionResolver().resolveVersion(dependency);
                    } catch (VersionResolveException e) {
                        logger.error("Could not resolve version of dependency " + dependency + ".", e);
                        continue;
                    }
                } else {
                    logger.error("Could not resolve version of dependency " + dependency + " since this is a component without a repository, but the version is not set.");
                    continue;
                }
            }

            var dependencyComponent = dependency.getComponent();
            if (dependencyComponent == null) {
                logger.error("Could not get component " + dependency + ".");
                continue;
            }

            if (!dependencyComponent.isLoaded()) {
                dependencyComponent.loadComponent();

                if (!dependencyComponent.isLoaded()) {
                    logger.error("Could not load component " + dependencyComponent + ".");
                    continue;
                }

                queue.addAll(dependencyComponent.getDependenciesFiltered());
            }
        }
    }

    private void crawlMulti2(Component component) {
        var loadCount = new AtomicInteger();
        var failCount = new AtomicInteger();
        var executorService = Executors.newFixedThreadPool(Settings.crawlThreads);
        var queue = new ConcurrentLinkedQueue<>(component.getDependenciesFiltered());
        var runningTasks = new ConcurrentHashMap<Dependency, Future<Void>>();
        var resolvedDependencies = Collections.synchronizedSet(new HashSet<Dependency>());

        Logger.startThreadLogging(Settings.crawlThreads);

        while (!queue.isEmpty()) {
            var dependency = queue.poll();

            if (resolvedDependencies.contains(dependency)) {
                logger.info("Skipping dependency " + dependency + " since it was already resolved.");
                waitUntilQueueIsNotEmpty(queue, runningTasks);
                continue;
            }

            resolvedDependencies.add(dependency);

            runningTasks.computeIfAbsent(dependency, dep -> executorService.submit(() -> crawlMulti2Helper(dep, queue, loadCount, failCount, runningTasks)));
            waitUntilQueueIsNotEmpty(queue, runningTasks);
        }
    }

    private void waitUntilQueueIsNotEmpty(ConcurrentLinkedQueue<Dependency> queue, ConcurrentHashMap<Dependency, Future<Void>> runningTasks) {
        if (queue.isEmpty())
            logger.info("Waiting for tasks to complete. Running tasks: " + runningTasks.size());
        while (true) {
            var entriesToRemove = new ArrayList<Dependency>();
            for (var entry : runningTasks.entrySet()) {
                if (entry.getValue().isDone()) {
                    entriesToRemove.add(entry.getKey());
                }
            }

            entriesToRemove.forEach(runningTasks::remove);

            if (!queue.isEmpty()) {
                return;
            }

            if (runningTasks.isEmpty()) {
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting for tasks to complete." + e);
                return;
            }
        }

    }

    private Void crawlMulti2Helper(Dependency dep, ConcurrentLinkedQueue<Dependency> queue, AtomicInteger loadCount, AtomicInteger failCount, ConcurrentHashMap<Dependency, Future<Void>> runningTasks) {
        if (dep == null) return null;

        if (!resolveVersion(dep, runningTasks)) return null;
        if (!loadComponent(dep.getComponent()).addDependenciesToQueue) {
            return null;
        }

        queue.addAll(dep.getComponent().getDependenciesFiltered());
        return null;

    }

    private boolean resolveVersion(Dependency dependency, ConcurrentHashMap<Dependency, Future<Void>> runningTasks) {
        if (dependency.hasVersion()) {
            return true;
        }

        var repository = dependency.getType().getRepository();
        if (repository == null) {
            logger.error("Could not resolve version of dependency " + dependency + " since this is a component without a repository.");
            return false;
        }

        try {
            var entry = runningTasks.remove(dependency);
            repository.getVersionResolver().resolveVersion(dependency);
            runningTasks.put(dependency, entry);
            return true;
        } catch (VersionResolveException e) {
            logger.error("Could not resolve version of dependency " + dependency + ": " + e);
            return false;
        }
    }

    private LoadingStatus loadComponent(Component component) {
        if (component.isLoaded()) {
            return LoadingStatus.PREVIOUS;
        }

        try {
            component.loadComponent();
            if (!component.isLoaded()) {
                logger.error("Failed to load component: " + component);
                return LoadingStatus.ERROR;
            }
            return LoadingStatus.SUCCESS;
        } catch (Exception e) {
            logger.error("Failed to load component: " + component, e);
            return LoadingStatus.ERROR;
        }
    }

    public void updateDependenciesToNewestVersion(Component rootComponent) {
        for (var dependency : rootComponent.getDependenciesFlatFiltered()) {
            if (dependency == null) continue;

            var dependencyComponent = dependency.getComponent();
            if (dependencyComponent == null) continue;

            System.out.println("Check version overwrite of " + dependencyComponent.getQualifiedName() + ".");

            if (dependencyComponent instanceof MavenComponent) {
                if (!dependency.isNotOptional() || !dependency.shouldResolveByScope()) {
                    logger.info("Skipping Maven Component " + dependencyComponent + " since it is optional or not resolved in the default scope.");
                    continue;
                }

                var components = dependencyComponent.getRepository().getLoadedComponents(dependencyComponent.getGroup(), dependencyComponent.getArtifactId());
                if (components.isEmpty()) {
                    logger.error("Could not find any loaded components for " + dependencyComponent.getQualifiedName() + ". This should never happen, as the component itself should be present with the repository");
                    continue;
                }

                var newestComponent = components.get(components.size() - 1);
                if (newestComponent.getVersion().compareTo(dependencyComponent.getVersion()) > 0) {
                    logger.info("Overwriting version of " + dependencyComponent.getQualifiedName() + " to " + newestComponent.getVersion() + " from " + dependencyComponent.getVersion() + ".");

                    dependency.setComponent(newestComponent);
                    dependency.setVersion(newestComponent.getVersion());
                    newestComponent.setData("addProperty", Property.of("overwritesDependencyVersion", dependencyComponent.getQualifiedName()));
                }

            } else {
                logger.info("Skipping component (and all dependencies of it) " + dependencyComponent + " since its not a maven component.");
            }
        }
    }

    enum LoadingStatus {
        SUCCESS(true), PREVIOUS(false), ERROR(false),
        ;

        private final Boolean addDependenciesToQueue;

        LoadingStatus(Boolean addDependenciesToQueue) {
            this.addDependenciesToQueue = addDependenciesToQueue;
        }
    }
}
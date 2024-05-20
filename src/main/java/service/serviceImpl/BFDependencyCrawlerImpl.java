package service.serviceImpl;

import data.Component;
import data.Dependency;
import exceptions.VersionResolveException;
import logger.Logger;
import service.BFDependencyCrawler;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {
    Logger logger = Logger.of("Crawler");

    public void crawl(Component parentComponent, boolean multiThreaded) {

        var time = System.currentTimeMillis();
        logger.infoLine("Crawling dependencies of " + parentComponent.getQualifiedName() + "...");
        int numberOfComponents = 0;
        if (multiThreaded) {
            crawlMulti(parentComponent);
        } else {
            numberOfComponents = crawlSingle(parentComponent);
        }

        double timeTaken = (System.currentTimeMillis() - time) / 1000.0 ;
        var format = new DecimalFormat("0.##");
        logger.successLine("Crawling finished in " + timeTaken  + "s. (" + format.format(timeTaken / numberOfComponents) + "s per component)");
    }

    public int crawlSingle(Component parentComponent) {
        int loadCount = 0;
        int failCount = 0;
        parentComponent.loadComponent();
        var repository = parentComponent.getRepository();
        var queue = new ArrayDeque<Dependency>(parentComponent.getDependencies());
        while (!queue.isEmpty()) {
            var dependency = queue.poll();
            //resolve the version of the dependency if we dont have done that yet
            if (!dependency.hasVersion()) {
                try {
                    repository.getVersionResolver().resolveVersion(dependency);
                } catch (VersionResolveException e) {
                    logger.errorLine(e.getMessage());
                }
            }
            var component = dependency.getComponent();
            if (component != null) {
                component.loadComponent();
                if (component.isLoaded()) {
                    var unloadedDependencies = component.getDependencies().stream().filter(d -> d.getComponent() == null).toList();
                    queue.addAll(unloadedDependencies);
                    loadCount++;
                } else {
                    failCount++;
                }
            }
        }
        logger.infoLine("Loaded " + loadCount + " components.");
        if (failCount > 0) logger.errorLine("Failed to load " + failCount + " components.");
        else logger.infoLine("All components loaded successfully.");
        return loadCount + failCount;
    }

    public void crawlMulti(Component parentComponent) {
        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        parentComponent.loadComponent();

        var repository = parentComponent.getRepository();
        var queue = new ArrayDeque<Dependency>(parentComponent.getDependencies());

        AtomicInteger processedCount = new AtomicInteger();
        while (!queue.isEmpty() || processedCount.get() > 0) {
            var dependency = queue.poll();
            executor.execute(() -> {
                processedCount.getAndIncrement();
                //resolve the version of the dependency if we dont have done that yet
                if (!dependency.hasVersion()) {
                    try {
                        repository.getVersionResolver().resolveVersion(dependency);
                    } catch (VersionResolveException e) {
                        logger.error(e.getMessage());
                    }
                }

                Component component = dependency.getComponent();
                if (component != null && !component.isLoaded()) {
                    component.loadComponent();
                    if (component.isLoaded()) {
                        var unloadedDependencies = component.getDependencies().stream().filter(d -> d.getComponent() == null).toList();
                        synchronized (queue) {
                            queue.addAll(unloadedDependencies);
                        }
                        loadCount.getAndIncrement();
                    } else {
                        failCount.getAndIncrement();
                    }
                }
                processedCount.getAndDecrement();
            });
        }
        executor.shutdown();

//        parentComponent.loadComponent();
//        var repository = parentComponent.getRepository();
//        var queue = new ArrayDeque<Dependency>(parentComponent.getDependencies());
//        while (!queue.isEmpty()) {
//            var dependency = queue.poll();
//
//            //resolve the version of the dependency if we dont have done that yet
//            if (!dependency.hasVersion()) {
//                try {
//                    repository.getVersionResolver().resolveVersion(dependency);
//                } catch (VersionResolveException e) {
//                    System.err.println(e.getMessage());
//                }
//            }
//
//            var component = dependency.getComponent();
//            if (component != null) {
//                component.loadComponent();
//                if (component.isLoaded()) {
//                    var unloadedDependencies = component.getDependencies().stream().filter(d -> d.getComponent() == null).toList();
//                    queue.addAll(unloadedDependencies);
//                    loadCount.getAndIncrement();
//                } else {
//                    failCount.getAndIncrement();
//                }
//
//            }
//        }


    }
}
package service.serviceImpl;

import data.Component;
import data.Dependency;
import exceptions.VersionResolveException;
import repository.Repository;
import service.BFDependencyCrawler;

import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {

    public void crawl(Component parentComponent, boolean multiThreaded) {
        if (multiThreaded) {
            crawlMulti(parentComponent);
        } else {
            crawlSingle(parentComponent);
        }
    }

    public void crawlSingle(Component parentComponent) {
        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
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
                    System.err.println(e.getMessage());
                }
            }
            var component = dependency.getComponent();
            if (component != null) {
                component.loadComponent();
                if (component.isLoaded()) {
                    var unloadedDependencies = component.getDependencies().stream().filter(d -> d.getComponent() == null).toList();
                    queue.addAll(unloadedDependencies);
                    loadCount.getAndIncrement();
                } else {
                    failCount.getAndIncrement();
                }
            }
        }
        System.out.println("\nLoaded " + loadCount + " components.\n" + "Failed to load " + failCount + " components.");
    }

    public void crawlMulti(Component parentComponent) {
        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        int numThreads = Runtime.getRuntime().availableProcessors();
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
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
                            System.err.println(e.getMessage());
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
        }

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


        System.out.println("\nLoaded " + loadCount + " components.\n" + "Failed to load " + failCount + " components.");
    }
}
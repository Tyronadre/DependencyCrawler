package service.serviceImpl;

import data.Component;
import data.Dependency;
import exceptions.VersionResolveException;
import logger.Logger;
import repository.ComponentRepository;
import service.BFDependencyCrawler;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {
    private static final Logger logger = Logger.of("DependencyCrawler");

    public void crawl(Component parentComponent, boolean multiThreaded) {

        var time = System.currentTimeMillis();
        logger.info("Crawling dependencies of " + parentComponent.getQualifiedName() + "...");
        int numberOfComponents;
        if (multiThreaded) {
            numberOfComponents = crawlMulti(parentComponent);
        } else {
            numberOfComponents = crawlSingle(parentComponent);
        }

        double timeTaken = (System.currentTimeMillis() - time) / 1000.0;
        var format = new DecimalFormat("0.##");
        logger.success("Crawling finished in " + timeTaken + "s. (" + format.format(timeTaken / numberOfComponents) + "s per component)");
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
                    logger.error(e.getMessage());
                }
            }
            var component = dependency.getComponent();
            if (component != null) {
                component.loadComponent();
                if (component.isLoaded()) {
                    for (var unloadedDependency : component.getDependencies()) {
                        if (unloadedDependency.shouldResolveByScope() && unloadedDependency.isNotOptional()) {
                            queue.add(unloadedDependency);
                        } else {
                            if (!unloadedDependency.isNotOptional())
                                logger.info("Dependency " + unloadedDependency + " is not resolved because it is optional.");
                            else if (!unloadedDependency.shouldResolveByScope())
                                logger.info("Dependency " + unloadedDependency + " is not resolved because it is of scope \"" + unloadedDependency.getScope() + "\".");
                        }
                    }
                    loadCount++;
                } else {
                    failCount++;
                }
            }
        }
        logger.success("Resolved " + loadCount + " components.");
        if (failCount > 0) logger.error("Failed to resolve " + failCount + " components.");
        else logger.success("All components loaded successfully.");
        return loadCount + failCount;
    }

    public int crawlMulti(Component parentComponent) {
        parentComponent.loadComponent();
        var repository = parentComponent.getRepository();

        AtomicInteger loadCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(numThreads * 2, 20));
        var queue = new ConcurrentLinkedDeque<Dependency>(parentComponent.getDependencies());
        var processing = Collections.synchronizedList(new ArrayList<Dependency>());

        while (true) {
            //get next dependency in queue
            Dependency dependency;
            dependency = queue.poll();

            //check if we are processing something, if yes wait and try again
            if (dependency == null) {
                if (processing.isEmpty()) {
                    break;
                }

                //wait a moment before checking again
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // add the dependency to the processing list
            processing.add(dependency);
            executorService.submit(() -> multiCrawlHelper(dependency, queue, processing, repository, loadCount, failCount));
        }

        executorService.shutdown();
        logger.success("Resolved " + loadCount + " components.");
        if (failCount.get() > 0) logger.error("Failed to resolve " + failCount + " components.");
        else logger.success("All components resolved successfully.");
        return loadCount.get() + failCount.get();
    }

    private Component multiCrawlHelper(Dependency dependency, ConcurrentLinkedDeque<Dependency> queue, List<Dependency> processing, ComponentRepository repository, AtomicInteger loadCount, AtomicInteger failCount) {
        try {
            //get the version of the dependency
            if (!dependency.hasVersion()) {
                try {
                    repository.getVersionResolver().resolveVersion(dependency);
                } catch (VersionResolveException e) {
                    logger.error(e.getMessage());
                }
            }

            //get the component of the dependency
            var component = dependency.getComponent();

            // process the component
            if (component != null)
                if (!component.isLoaded()) {

                    component.loadComponent();

                    if (component.isLoaded()) {
                        queue.addAll(component.getDependencies().stream().filter(Dependency::shouldResolveByScope).filter(Dependency::isNotOptional).toList());

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

        } finally {
            // remove the component from the processing list
            processing.remove(dependency);
        }
        return dependency.getComponent();
    }
}
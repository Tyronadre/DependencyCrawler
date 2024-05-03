package service.serviceImpl;

import data.Component;
import data.Dependency;
import exceptions.VersionResolveException;
import service.BFDependencyCrawler;

import java.util.ArrayDeque;

public class BFDependencyCrawlerImpl implements BFDependencyCrawler {
    @Override
    public void loadDependencies(Component parentComponent) {
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
                var unloadedDependencies = component.getDependencies().stream().filter(d -> d.getComponent() == null).toList();
                queue.addAll(unloadedDependencies);
            }
        }
    }

}

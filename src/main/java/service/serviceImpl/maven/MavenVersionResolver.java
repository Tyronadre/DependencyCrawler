package service.serviceImpl.maven;

import data.Dependency;
import data.dataImpl.maven.MavenComponent;
import data.dataImpl.maven.MavenDependency;
import data.dataImpl.maven.MavenVersion;
import exceptions.VersionRangeResolutionException;
import exceptions.VersionResolveException;
import repository.repositoryImpl.MavenRepository;
import service.VersionResolver;

public class MavenVersionResolver implements VersionResolver {
    private final MavenRepository repository;

    public MavenVersionResolver(MavenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void resolveVersion(Dependency dependency) throws VersionResolveException {
        var version = resolveVersion(dependency.getVersionConstraints(), dependency, (MavenComponent) dependency.getTreeParent());
        if (version != null) {
            dependency.setVersion(version);
        } else {
            throw new VersionResolveException(dependency, "Could not resolve version.");
        }
    }

    private MavenVersion resolveVersion(String versionString, Dependency dependency, MavenComponent parent) {
        var mavenDependency = (MavenDependency) dependency;

        if (versionString == null || versionString.isBlank()) {
            //check dependency management of the pom that is using this dependency (parent should be treeParent)
            var dependencyManagement = parent.getDependencyManagement();
            if (dependencyManagement != null) {
                //get the managed dependency
                var managedDependency = dependencyManagement.getDependencies().stream().filter(d ->
                        (d.getGroupId().equals(mavenDependency.getGroupId()) || d.getGroupId().equals("${project.groupId}") || d.getGroupId().equals("${pom.groupId}"))
                                && d.getArtifactId().equals(mavenDependency.getArtifactId()))
                        .findFirst().orElse(null);
                if (managedDependency != null) {
                    return resolveVersion(managedDependency.getVersion(), dependency, parent);
                }
            }
            //check parent pom
            if (parent.getParent() != null) {
                if (!parent.getParent().isLoaded()) parent.getParent().loadComponent();
                return resolveVersion(parent.getParent().getProperty(mavenDependency.getArtifactId() + ".version"), dependency, (MavenComponent) parent.getParent());
            }
        } else if (versionString.startsWith("[") || versionString.startsWith("(")) {
            return getVersionFromVersionRange(dependency);
        } else if (versionString.startsWith("$")) {
            return getVersionFromProperty(versionString.substring(2, versionString.length() - 1), dependency, parent);
        } else {
            return getVersion(versionString);
        }
        return null;
    }

    private MavenVersion getVersionFromVersionRange(Dependency dependency) {
        try {
            return (MavenVersion) repository.getVersionRangeResolver().resolveVersionRange(dependency).getRecommendedVersion();
        } catch (VersionRangeResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    private MavenVersion getVersionFromProperty(String substring, Dependency dependency, MavenComponent parent) {
        //project.version
        if (substring.equals("project.version") || substring.equals("pom.version") || substring.equals("parent.version")) {
            return new MavenVersion(parent.getVersion().getVersion());
        }

        var property = parent.getProperty(substring);
        if (property == null && parent.getParent() != null) {
            if (!parent.getParent().isLoaded()) parent.getParent().loadComponent();
            return getVersionFromProperty(substring, dependency, (MavenComponent) parent.getParent());
        } else if (property == null) {
            System.err.println("Warning: Could not resolve version for: " + dependency);
            return null;
        }
        return resolveVersion(property, dependency, parent);
    }


    @Override
    public MavenVersion getVersion(String versionString) {
        return new MavenVersion(versionString);
    }
}

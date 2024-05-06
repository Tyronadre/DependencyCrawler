package repository;

import data.Component;
import data.Dependency;
import data.Version;
import enums.RepositoryType;
import service.VersionRangeResolver;
import service.VersionResolver;

import java.util.List;

public interface Repository {
    String request(String request);

    /**
     * Returns a list of all possible versions for this dependency.
     *
     * @param dependency the dependency
     */
    List<? extends Version> getVersions(Dependency dependency);

    /**
     * Returns a list of all possible versions for this artifact.
     *
     * @param component the artifact
     */
    List<? extends Version> getVersions(Component component);

    VersionResolver getVersionResolver();

    /**
     * Loads all data for a component.
     *
     * @param component the component request
     * @return true if the component was loaded successfully, false otherwise
     */
    boolean loadComponent(Component component);

    /**
     * Returns a component from the repository if it exists. Otherwise, returns null.
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     * @return the component or null
     */
    Component getComponent(String groupId, String artifactId, Version version);

    VersionRangeResolver getVersionRangeResolver();

    RepositoryType getType();
}

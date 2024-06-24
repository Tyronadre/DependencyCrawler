package repository;

import data.Component;
import data.Dependency;
import data.Version;
import service.VersionResolver;

import java.util.List;

public interface ComponentRepository {
    /**
     * Returns a list of all possible versions for this dependency.
     *
     * @param dependency the dependency
     */
    List<? extends Version> getVersions(Dependency dependency);

    /**
     *
     * @return the version resolver for this repository
     */
    VersionResolver getVersionResolver();

    /**
     * Loads all data for a component.
     *
     * @param component the component request
     * @return 0 if the component was loaded successfully, 1 if the component was not found in the repository, 2 if the model could not be parsed
     */
    int loadComponent(Component component);

    /**
     * Returns a component from the repository if it exists. Otherwise, returns null.
     * Returns the component with the highest version if multiple components with group and artifact id exist.
     * Needs to be synchronized!
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     * @param parent
     * @return the component or null
     */
    Component getComponent(String groupId, String artifactId, Version version, Component parent);

    String getDownloadLocation(Component component);

    /**
     * Returns an order list of all loaded components with the given groupName and artifactName.
     * The list is sorted by the version, where the first element is the earliest version, and the last element is the newest version.
     *
     * @param groupName    the group name
     * @param artifactName the artifact name
     * @return the sorted list
     */
    List<Component> getLoadedComponents(String groupName, String artifactName);

}

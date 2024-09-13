package repository.repositoryImplNew;

import data.Version;
import dataNew.Component;
import dataNew.Dependency;

import javax.annotation.Nullable;
import java.util.List;

public interface NewComponentRepository {

    /**
     * Returns the version for this dependency.
     *
     * @param dependency the dependency
     */
    Version resolveVersion(Dependency dependency);


    /**
     * Loads all data for a component.
     *
     * @param component the component request
     * @return 0 if the component was loaded successfully, 1 if the component was not found in the repository, 2 if the model could not be parsed
     */
    LoadingStatus loadComponent(Component component);

    /**
     * Returns a component from the repository if it exists. Otherwise, returns null.
     * Returns the component with the highest version if multiple components with group and artifact id exist.
     * Needs to be synchronized!
     *
     * @param groupId    the group id. Can be null depending on the component type
     * @param artifactId the artifact id
     * @param version    the version
     * @param parent     a parent in the tree. note that components have to be unique, and a component can have multiple parents. this is only used so a component can load data from a parent.
     * @return the component or null
     */
    Component getComponent(@Nullable String groupId, String artifactId, Version version, Component parent);

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

    /**
     * Returns all loaded components.
     */
    List<Component> getLoadedComponents();

    public enum LoadingStatus {
        SUCCESS,
        NOT_FOUND,
        ERROR,
        PROCESSING
    }
}

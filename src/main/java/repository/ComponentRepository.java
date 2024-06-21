package repository;

import cyclonedx.sbom.Bom16;
import data.Component;
import data.Dependency;
import data.Version;
import enums.RepositoryType;
import repository.repositoryImpl.MavenComponentRepositoryType;
import repository.repositoryImpl.ReadComponentRepository;
import service.VersionRangeResolver;
import service.VersionResolver;

import java.util.List;
import java.util.TreeSet;

public interface ComponentRepository {

    static ComponentRepository of(RepositoryType repositoryType) {
        return MavenComponentRepositoryType.of((MavenComponentRepositoryType) repositoryType);
    }

    static Component getReadComponent(Bom16.Property bomRef) {
        return null;
    }

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
     *
     * @return the version range resolver for this repository
     */
    VersionRangeResolver getVersionRangeResolver();

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
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     * @return the component or null
     */
    Component getComponent(String groupId, String artifactId, Version version);

    String getDownloadLocation(Component component);

    static TreeSet<Component> getLoadedComponents(String groupName, String artifactName){
        var set = MavenComponentRepositoryType.getLoadedComponents(groupName, artifactName);
        set.addAll(ReadComponentRepository.getInstance().getLoadedComponents(groupName, artifactName));
        return set;
    }

}

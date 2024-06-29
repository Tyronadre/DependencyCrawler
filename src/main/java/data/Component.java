package data;

import repository.ComponentRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A component in a repository.
 * <p>
 * A component should be loaded with {@link #loadComponent()} before any other method is called, when any data from this component is needed.
 */
public interface Component {
    /**
     * Loads all data of this component from the repository.
     * If no repository is set, all available repositories should be tried, and one that has the component should be set.
     * All other methods may not work correctly before this method was called.
     * <p>
     * This method has to be thread safe for the default implementation of the crawler
     */
    void loadComponent();

    /**
     * @return true if the component is loaded, false otherwise
     */
    boolean isLoaded();

    /**
     * @return the dependencies of the artifact
     */
    List<Dependency> getDependencies();

    /**
     * Only dependencies that are resolved are returned.
     *
     * @return the filtered dependencies of the artifact.
     */
    default List<Dependency> getDependenciesFiltered() {
        return getDependencies().stream()
                .filter(Objects::nonNull)
                .filter(Dependency::shouldResolveByScope)
                .filter(Dependency::isNotOptional)
                .sorted(Comparator.comparing(Dependency::getQualifiedName))
                .collect(Collectors.toList());    }

    /**
     * @return the name of the artifact
     */
    String getQualifiedName();

    String getGroup();

    String getArtifactId();

    /**
     * @return the version of the artifact
     */
    Version getVersion();

    /**
     * @return the organization that supplies the artifact
     */
    Organization getSupplier();

    /**
     * @return the organization that manufactures the artifact
     */
    Organization getManufacturer();

    /**
     * @return the contributors of the artifact
     */
    List<Person> getContributors();

    /**
     * @return the description of the artifact
     */
    String getDescription();

    /**
     * @return the repository of the artifact
     */
    ComponentRepository getRepository();

    /**
     * @return the purl of the artifact
     */
    String getPurl();

    /**
     * Returns the value of the property with the given key.
     *
     * @param key the key of the property
     * @return the value of the property
     */
    String getProperty(String key);

    /**
     * The parent of this Component (eg. specified in a bom file)
     *
     * @return the parent of this Component
     */
    Component getParent();

    /**
     * Adds a dependency to this component
     *
     * @param dependency the dependency to add
     */
    void addDependency(Dependency dependency);

    /**
     * Sets this as a root component
     */
    void setRoot();

    List<ExternalReference> getAllExternalReferences();

    List<Hash> getAllHashes();

    List<Vulnerability> getAllVulnerabilities();

    String getDownloadLocation();

    /**
     * @return a filtered list of dependencies, that only contains the dependencies that are resolved in alphabetical order
     */
    default List<Dependency> getDependenciesFlatFiltered() {
        return getDependencies().stream()
                .filter(Dependency::shouldResolveByScope)
                .filter(Dependency::isNotOptional)
                .flatMap(dependency -> Stream.concat(
                        Stream.of(dependency),
                        dependency.getComponent() != null && dependency.getComponent().isLoaded()
                                ? dependency.getComponent().getDependenciesFlatFiltered().stream()
                                : Stream.empty()
                ))
                .distinct()
                .sorted(Comparator.comparing(Dependency::getQualifiedName))
                .collect(Collectors.toList());
    }

    /**
     * @return a filtered list of components, that only contains the components that are resolved in alphabetical order
     */
    default List<Component> getDependencyComponentsFlatFiltered() {
        return this.getDependenciesFlatFiltered().stream()
                .distinct()
                .map(Dependency::getComponent)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(Component::getQualifiedName))
                .toList();
    }

    String getPublisher();

    List<LicenseChoice> getAllLicenses();

    List<Property> getAllProperties();

    List<Person> getAllAuthors();

    /**
     * Sets the data for the given key.
     * Exact implementation is up to the component.
     *
     * @param key   the key
     * @param value the value
     * @param <T>   the type of the value
     */
    <T> void setData(String key, T value);

    void removeDependency(Dependency dependency);

    void removeVulnerability(Vulnerability vulnerability);

    void addVulnerability(Vulnerability vulnerability);
}


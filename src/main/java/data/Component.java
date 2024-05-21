package data;

import cyclonedx.v1_6.Bom16;
import data.dataImpl.maven.MavenComponent;
import repository.Repository;
import repository.repositoryImpl.MavenRepository;

import java.util.Arrays;
import java.util.List;

/**
 * A component in a repository.
 * <p>
 * A component should be loaded with {@link #loadComponent()} before any other method is called, when any data from this component is needed.
 */
public interface Component extends Bom16Component<Bom16.Component> {
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
    List<? extends Dependency> getDependencies();

    /**
     * @return the name of the artifact
     */
    String getQualifiedName();

    String getGroup();

    String getName();

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
     *
     * @return  the contributors of the artifact
     */
    List<Person> getContributors();

    /**
     *
     * @return the description of the artifact
     */
    String getDescription();

    /**
     *
     * @return the homepage of the artifact
     */
    String getHomepage();

    /**
     *
     * @return the repository of the artifact
     */
    Repository getRepository();

    /**
     *
     * @return the purl of the artifact
     */
    String getPurl();

    /**
     *
     * @return the properties of the artifact
     */
    List<String> getOniborIds();

    /**
     *
     * @return the properties of the artifact
     */
    List<String> getAllSwhIds();

    /**
     * Returns the value of the property with the given key.
     *
     * @param key the key of the property
     * @return the value of the property
     */
    String getProperty(String key);

    /**
     * The parent of this Component (eg. specified in a bom file)
     * @return the parent of this Component
     */
    Component getParent();


    /**
     * Sets the repository of this component
     * @param mavenRepository the repository
     */
    void setRepository(MavenRepository mavenRepository);

    /**
     * Adds a dependency to this component
     * @param dependency the dependency to add
     */
    void addDependency(Dependency dependency);

    /**
     * Sets this as a root component
     */
    void setRoot();

    void printTree(String s);

    List<ExternalReference> getAllExternalReferences();

    List<Hash> getAllHashes();

    List<License> getAllLicences();

    List<Vulnerability> getAllVulnerabilites();
}

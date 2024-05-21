package data;

import cyclonedx.v1_6.Bom16;

/**
 * Represents a dependency of an artifact.
 */
public interface Dependency extends Bom16Component<Bom16.Dependency> {
    String getName();

    /**
     * The version of the dependency.
     *
     * @return the version of the dependency or null if the dependency has no resolved version
     */
    Version getVersion();

    /**
     * The scope of the dependency.
     *
     * @return the scope of the dependency
     */
    String getScope();

    /**
     * The component of the dependency.
     * If the dependency component is not yet resolved, it will try to resolve the component first.
     *
     * @return the component of the dependency
     */
    Component getComponent();

    /**
     * The parent component of the dependency in the dependency tree.
     *
     * @return the treeParent component of the dependency
     */
    Component getTreeParent();

    /**
     * Returns the version constraints.
     *
     * @return the version constraints
     */
    String getVersionConstraints();

    /**
     * @return true if the dependency has a version, false otherwise
     */
    boolean hasVersion();

    /**
     * Sets the version of the dependency.
     *
     * @param version the version of the dependency
     */
    void setVersion(Version version);

    /**
     * Sets the component of the dependency.
     *
     * @param component the component of the dependency
     */
    void setComponent(Component component);

    /**
     * @return true if the dependency is optional, false otherwise
     */
    boolean getOptional();
}
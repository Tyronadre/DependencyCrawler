package data.dataImpl.maven;

import cyclonedx.v1_6.Bom16;
import data.Component;
import data.Dependency;
import data.Version;

public class MavenDependency implements Dependency {
    private final MavenComponent treeParent; //The component that has this dependency
    private MavenComponent parent; //The parent of this dependency (specified in the pom file)
    private MavenComponent component;
    private final String groupId;
    private final String artifactId;
    private String versionConstraints;
    private MavenVersion version;
    private final String scope;
    private final Boolean optional;

    public MavenDependency(String groupId, String artifactId, Version version, MavenComponent treeParent) {
        this.component = (MavenComponent) treeParent.getRepository().getComponent(groupId, artifactId, version);
        this.component = new MavenComponent(groupId, artifactId, version, treeParent.getRepository());
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.treeParent = treeParent;
        this.version = (MavenVersion) version;
        this.optional = false;
        this.scope = "compile";
    }

    /**
     * Constructor for a MavenDependency without a resolved version.
     * The version should be resolved later from the calling function, but will be resolved if {@link #getVersion()} is called.
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param treeParent     the parent component
     */
    public MavenDependency(String groupId, String artifactId, String versionConstraints, String scope, String optional,  MavenComponent treeParent) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.treeParent = treeParent;
        this.scope = scope == null ? "" : scope;
        this.optional = Boolean.parseBoolean(optional);
        this.versionConstraints = versionConstraints;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getName() {
        return groupId + ":" + artifactId + ":" + versionConstraints;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public MavenComponent getComponent() {
        if (component == null && version != null) {
            this.component = (MavenComponent) treeParent.getRepository().getComponent(getGroupId(), getArtifactId(), getVersion());
        } else if (component == null) {
            return null;
        }
        return component;
    }

    @Override
    public MavenComponent getTreeParent() {
        return treeParent;
    }

    @Override
    public String getVersionConstraints() {
        return versionConstraints;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + versionConstraints;
    }

    @Override
    public boolean hasVersion() {
        return component != null && component.getVersion() != null;
    }

    @Override
    public void setVersion(Version version) {
        this.version = (MavenVersion) version;
    }

    @Override
    public void setComponent(Component component) {
        this.component = (MavenComponent) component;
        this.version = (MavenVersion) component.getVersion();
    }

    @Override
    public boolean getOptional() {
        return optional;
    }

    @Override
    public Bom16.Dependency toBom16() {
        var builder = Bom16.Dependency.newBuilder();
        builder.setRef(this.component.getBomRef());
        builder.addAllDependencies(this.component.getDependencies().stream().map(Dependency::toBom16).toList());
        return builder.build();
    }
}

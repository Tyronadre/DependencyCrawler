package data.dataImpl.maven;

import cyclonedx.v1_6.Bom16;
import data.Component;
import data.Dependency;
import data.Version;
import logger.AppendingLogger;
import logger.Logger;

import java.util.Objects;

public class MavenDependency implements Dependency {
    private static final Logger logger = Logger.of("MavenDependency");

    private final MavenComponent treeParent; //The component that has this dependency
    private MavenComponent parent; //The parent of this dependency (specified in the pom file)
    private MavenComponent component;
    private final String groupId;
    private final String artifactId;
    private String versionConstraints;
    private MavenVersion version;
    private final String scope;
    private final Boolean optional;

    /**
     * Constructor for a Maven Dependency with a resolved version.
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     * @param treeParent the parent component
     */
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
     * The version has to be set, if the component should be available.
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param treeParent the parent component
     */
    public MavenDependency(String groupId, String artifactId, String versionConstraints, String scope, String optional, MavenComponent treeParent) {
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
    public synchronized MavenComponent getComponent() {
        if (component == null && version != null) {
            this.component = (MavenComponent) treeParent.getRepository().getComponent(getGroupId(), getArtifactId(), getVersion());
        } else if (component == null) {
            logger.error("Can not get Component of Dependency " + this + ". Parent is: " + this.treeParent + ". [Version is not resolved]");
            return null;
        }
        return component;
    }

    public Boolean shouldResolveByScope() {
        return !(this.scope.equals("test") || this.scope.equals("provided") || this.scope.equals("system") || this.scope.equals("import") || this.scope.equals("runtime"));
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
        if (versionConstraints == null)
            return groupId + ":" + artifactId + ":" + version;
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
    public boolean isNotOptional() {
        return !optional;
    }

    @Override
    public Bom16.Dependency toBom16() {
        if (this.component == null) return null;
        var builder = Bom16.Dependency.newBuilder();
        builder.setRef(this.component.getQualifiedName() + "_" + this.getScope());
        this.component.getDependencies().stream().filter(MavenDependency::shouldResolveByScope).filter(MavenDependency::isNotOptional).forEach(
                d -> {
                    if (d.getComponent() != null) builder.addDependencies(d.toBom16());
                }
        );
        return builder.build();
    }
}

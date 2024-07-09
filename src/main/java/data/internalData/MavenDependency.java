package data.internalData;

import data.Component;
import data.Dependency;
import data.Version;
import logger.Logger;
import repository.repositoryImpl.MavenComponentRepository;

import java.util.Objects;

public class MavenDependency implements Dependency {
    private static final Logger logger = Logger.of("MavenDependency");

    private final Component treeParent; //The component that has this dependency
    private Component component;
    private final String groupId;
    private final String artifactId;
    private String versionConstraints;
    private Version version;
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
        this.component = treeParent.getRepository().getComponent(groupId, artifactId, version, treeParent);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.treeParent = treeParent;
        this.version = version;
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
    public MavenDependency(String groupId, String artifactId, String versionConstraints, String scope, String optional, Component treeParent) {
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
    public String getQualifiedName() {
        var name = "";
        if (component != null) name = component.getQualifiedName();
        else if (version != null)  name = groupId + ":" + artifactId + ":" + version;
        else name = groupId + ":" + artifactId + ":" + versionConstraints;
        return name;
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
    public synchronized Component getComponent() {
        if (component == null && version != null) {
            this.component = treeParent.getRepository().getComponent(getGroupId(), getArtifactId(), getVersion(), null);
        } else if (component == null) {
//            logger.error("Can not get Component of Dependency " + this + ". Parent is: " + this.treeParent + ". [Version is not resolved].");
            var possibleComponents = MavenComponentRepository.getInstance().getLoadedComponents(getGroupId(), getArtifactId());
            if (!possibleComponents.isEmpty()) {
                logger.info("Can not get Component of Dependency " + this + ". Parent is: " + this.treeParent + ". [Version is not resolved]." + "Using " + possibleComponents.get(possibleComponents.size() - 1) + " as fallback.");
                this.component = possibleComponents.get(possibleComponents.size() - 1);
            }
        }
        return this.component;
    }

    public Boolean shouldResolveByScope() {
        return !(this.scope.equals("test") || this.scope.equals("provided") || this.scope.equals("system") || this.scope.equals("import") || this.scope.equals("runtime"));
    }

    @Override
    public Component getTreeParent() {
        return treeParent;
    }

    @Override
    public String getVersionConstraints() {
        return versionConstraints;
    }

    @Override
    public String toString() {
        if (version != null)
            return groupId + ":" + artifactId + ":" + version;
        return groupId + ":" + artifactId + ":" + versionConstraints;
    }

    @Override
    public boolean hasVersion() {
        return component != null && component.getVersion() != null;
    }

    @Override
    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public void setComponent(Component component) {
        this.component = component;
        this.version = component.getVersion();
    }

    @Override
    public boolean isNotOptional() {
        return true;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MavenDependency that)) return false;

        return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(groupId);
        result = 31 * result + Objects.hashCode(artifactId);
        result = 31 * result + Objects.hashCode(version);
        return result;
    }
}

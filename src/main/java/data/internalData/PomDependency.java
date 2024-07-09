package data.internalData;

import data.Component;
import data.Dependency;
import data.Version;

public class PomDependency implements Dependency {
    String groupId;
    String artifactId;
    String versionConstraints;
    Version version;

    public PomDependency(String groupId, String artifactId, String versionConstraints) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionConstraints = versionConstraints;
    }

    @Override
    public String getQualifiedName() {
        return groupId + ":" + artifactId + ":" + versionConstraints;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public Boolean shouldResolveByScope() {
        return null;
    }

    @Override
    public Component getTreeParent() {
        return null;
    }

    @Override
    public String getVersionConstraints() {
        return "";
    }

    @Override
    public boolean hasVersion() {
        return false;
    }

    @Override
    public void setVersion(Version version) {

    }

    @Override
    public void setComponent(Component component) {

    }

    @Override
    public boolean isNotOptional() {
        return false;
    }

}

package data.internalData;

import data.Component;
import data.Dependency;
import data.Version;
import repository.repositoryImpl.JitPackComponentRepository;

public class JitPackDependency implements Dependency {
    Component component;
    Component treeParent;

    public JitPackDependency(String groupId, String name, Version version, Component treeParent) {
        this.component = JitPackComponentRepository.getInstance().getComponent(groupId, name, version, treeParent);
    }

    @Override
    public String getQualifiedName() {
        return component.getQualifiedName();
    }

    @Override
    public Version getVersion() {
        return component.getVersion();
    }

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public Component getComponent() {
        return component;
    }

    @Override
    public Boolean shouldResolveByScope() {
        return true;
    }

    @Override
    public Component getTreeParent() {
        return treeParent;
    }

    @Override
    public String getVersionConstraints() {
        return null;
    }

    @Override
    public boolean hasVersion() {
        return true;
    }

    @Override
    public void setVersion(Version version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setComponent(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNotOptional() {
        return true;
    }

}

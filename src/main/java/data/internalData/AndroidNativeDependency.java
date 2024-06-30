package data.internalData;

import data.Component;
import data.Dependency;
import data.Version;
import repository.repositoryImpl.AndroidNativeComponentRepository;

public class AndroidNativeDependency implements Dependency {
    Component treeParent;
    Component component;

    public AndroidNativeDependency(String groupId, String name, Version version, Component treeParent) {
        this.component = AndroidNativeComponentRepository.getInstance().getComponent(groupId, name, version, treeParent);
        this.treeParent = treeParent;

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

    @Override
    public void setScope(String scope) {
        throw new UnsupportedOperationException();
    }
}

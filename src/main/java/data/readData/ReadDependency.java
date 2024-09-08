package data.readData;

import data.Component;
import data.Dependency;
import data.Version;
import enums.ComponentType;

public class ReadDependency implements Dependency {
    Component treeParent;
    Component component;

    public ReadDependency(Component component, Component treeParent) {
        this.treeParent = treeParent;
        this.component = component;
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
        throw new IllegalArgumentException();
    }

    @Override
    public void setComponent(Component component) {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isNotOptional() {
        return true;
    }

    @Override
    public String toString(){
        return getQualifiedName();
    }

    @Override
    public ComponentType getType() {
        return ComponentType.READ;
    }
}

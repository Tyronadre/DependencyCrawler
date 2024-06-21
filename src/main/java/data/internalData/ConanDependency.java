package data.internalData;

import data.Component;
import data.Dependency;
import data.Version;
import repository.repositoryImpl.ConanRepository;

public class ConanDependency implements Dependency {
    String name;
    Version version;
    Component parent;
    Component component;

    public ConanDependency( String name, Version version, Component parent) {
        this.name = name;
        this.version = version;
        this.parent = parent;
    }


    @Override
    public String getQualifiedName() {
        return name + ":" + version;
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
        if (component == null) {
            ConanRepository.getInstance().getComponent(null, name, version);
        }
        return component;
    }

    @Override
    public Boolean shouldResolveByScope() {
        return true;
    }

    @Override
    public Component getTreeParent() {
        return parent;
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
        this.component = component;
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

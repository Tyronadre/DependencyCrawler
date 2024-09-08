package data.internalData;

import data.Component;
import data.Dependency;
import data.Version;
import enums.ComponentType;
import repository.repositoryImpl.ConanComponentRepository;

import java.util.StringJoiner;

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
            component = ConanComponentRepository.getInstance().getComponent(null, name, version, null);
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
        this.version = version;
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
    public ComponentType getType() {
        return ComponentType.CONAN;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ConanDependency.class.getSimpleName() + "[", "]")
                .add("version=" + version)
                .add("name='" + name + "'")
                .toString();
    }
}

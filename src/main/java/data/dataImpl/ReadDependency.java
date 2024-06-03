package data.dataImpl;

import data.Component;
import data.Dependency;
import data.Version;

import java.util.Objects;

public class ReadDependency implements Dependency {
    Component component;
    Component parent;
    String ref;

    public ReadDependency(Component component,  Component parent) {
        this.component = component;
        this.parent = parent;
    }

    public ReadDependency(String ref, Component parent) {
        this.parent = parent;
        this.ref = ref;
    }

    @Override
    public String getQualifiedName() {
        if (ref != null)
            return ref;
        return component.getQualifiedName() ;
    }

    @Override
    public Version getVersion() {
        if (ref != null)
            return ref.split(":").length > 2 ? Version.of(ref.split(":")[2]) : null;
        return component.getVersion();
    }

    @Override
    public String getScope() {
        return "EXCLUDED";
    }

    @Override
    public Component getComponent() {
        return component;
    }

    @Override
    public Boolean shouldResolveByScope() {
        return ref == null;
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
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setComponent(Component component) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isNotOptional() {
        return true;
    }

    @Override
    public void setScope(String scope) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        return "Dependency: " + getQualifiedName();
    }

    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadDependency that)) return false;

        return Objects.equals(this.getQualifiedName(), that.getQualifiedName());
    }
}

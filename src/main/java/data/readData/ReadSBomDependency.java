package data.readData;

import cyclonedx.sbom.Bom16;
import data.Component;
import data.Dependency;
import data.Version;

import java.util.Objects;

public class ReadSBomDependency implements Dependency {
    Component component;
    Component parent;
    String ref;
    Bom16.Dependency dependency;

    public ReadSBomDependency(Bom16.Dependency dependency, Component component, Component parent) {
        this.dependency = dependency;
        this.parent = parent;
        if (component == null)
            this.ref = dependency.getRef();
        else
            this.component = component;
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
        if (!(o instanceof ReadSBomDependency that)) return false;

        return Objects.equals(this.getQualifiedName(), that.getQualifiedName());
    }

}

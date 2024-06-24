package data.internalData;

import data.Component;
import data.License;
import data.LicenseCollision;

import java.util.StringJoiner;

public class LicenseCollisionImpl implements LicenseCollision {
    License parentLicense;
    Component parentComponent;
    License childLicense;
    Component childComponent;
    String cause;

    public LicenseCollisionImpl(License parentLicense, Component parentComponent, License childLicense, Component childComponent, String cause) {
        this.parentLicense = parentLicense;
        this.parentComponent = parentComponent;
        this.childLicense = childLicense;
        this.childComponent = childComponent;
        this.cause = cause;
    }

    @Override
    public License getParentLicense() {
        return parentLicense;
    }

    @Override
    public Component getParent() {
        return parentComponent;
    }

    @Override
    public License getChildLicense() {
        return childLicense;
    }

    @Override
    public Component getChild() {
        return childComponent;
    }

    @Override
    public String getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LicenseCollisionImpl.class.getSimpleName() + "[", "]")
                .add("parentLicense=" + parentLicense.getNameOrId())
                .add("parentComponent=" + parentComponent.getQualifiedName())
                .add("childLicense=" + childLicense.getNameOrId())
                .add("childComponent=" + childComponent.getQualifiedName())
                .add("cause='" + cause + "'")
                .toString();
    }
}

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
    public License parentLicense() {
        return parentLicense;
    }

    @Override
    public Component parent() {
        return parentComponent;
    }

    @Override
    public License childLicense() {
        return childLicense;
    }

    @Override
    public Component child() {
        return childComponent;
    }

    @Override
    public String cause() {
        return cause;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LicenseCollisionImpl.class.getSimpleName() + "[", "]")
                .add("parentLicense=" + parentLicense.nameOrId())
                .add("parentComponent=" + parentComponent.getQualifiedName())
                .add("childLicense=" + childLicense.nameOrId())
                .add("childComponent=" + childComponent.getQualifiedName())
                .add("cause='" + cause + "'")
                .toString();
    }
}

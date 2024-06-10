package data.internalData;

import data.Component;
import data.License;
import data.LicenseCollision;

public class LicenseCollisionImpl implements LicenseCollision {
    License license1;
    Component component1;
    License license2;
    Component component2;
    String cause;

    public LicenseCollisionImpl(License license1, Component component1, License license2, Component component2, String cause) {
        this.license1 = license1;
        this.component1 = component1;
        this.license2 = license2;
        this.component2 = component2;
        this.cause = cause;
    }

    @Override
    public License getLicense1() {
        return license1;
    }

    @Override
    public Component getComponent1() {
        return component1;
    }

    @Override
    public License getLicense2() {
        return license2;
    }

    @Override
    public Component getComponent2() {
        return component2;
    }

    @Override
    public String getCause() {
        return cause;
    }
}

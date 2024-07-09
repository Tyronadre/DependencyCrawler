package data;

public interface LicenseCollision {

    License parentLicense();

    Component parent();

    License childLicense();

    Component child();

    String cause();

    static LicenseCollision of(License parentLicense, Component parent, License childLicense, Component child, String cause) {
        return new LicenseCollisionRecord(parentLicense, parent, childLicense, child, cause);
    }

    record LicenseCollisionRecord(License parentLicense, Component parent, License childLicense, Component child, String cause) implements LicenseCollision {
    }

}

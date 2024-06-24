package data;


public interface LicenseCollision {
    License getParentLicense();

    Component getParent();

    License getChildLicense();

    Component getChild();
    String getCause();
}

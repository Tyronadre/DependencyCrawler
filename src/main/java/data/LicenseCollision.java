package data;

public interface LicenseCollision {

    License getLicense1();
    Component getComponent1();
    License getLicense2();
    Component getComponent2();
    String getCause();
}

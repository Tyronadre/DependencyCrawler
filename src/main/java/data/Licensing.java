package data;

import java.util.List;

public interface Licensing {
    List<String> getAltIds();
    OrganizationOrPerson getLicensor();
    OrganizationOrPerson getLicensee();
    OrganizationOrPerson getPurchaser();
    String getPurchaseOrder();
    List<String> getAllLicenseTypes();
    Timestamp getLastRenewal();
    Timestamp getExpiration();
}
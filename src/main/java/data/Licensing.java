package data;

import java.util.List;

public interface Licensing {

    List<String> altIds();

    OrganizationOrPerson licensor();

    OrganizationOrPerson licensee();

    OrganizationOrPerson purchaser();

    String purchaseOrder();

    List<String> allLicenseTypes();

    Timestamp lastRenewal();

    Timestamp expiration();

    static Licensing of(List<String> altIds, OrganizationOrPerson licensor, OrganizationOrPerson licensee, OrganizationOrPerson purchaser, String purchaseOrder, List<String> allLicenseTypes, Timestamp lastRenewal, Timestamp expiration) {
        return new LicensingRecord(altIds, licensor, licensee, purchaser, purchaseOrder, allLicenseTypes, lastRenewal, expiration);
    }

    record LicensingRecord(List<String> altIds, OrganizationOrPerson licensor, OrganizationOrPerson licensee, OrganizationOrPerson purchaser, String purchaseOrder, List<String> allLicenseTypes, Timestamp lastRenewal, Timestamp expiration) implements Licensing {
    }

}
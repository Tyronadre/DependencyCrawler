package converter.cycloneDX;

import converter.Converter;
import data.Address;
import org.cyclonedx.model.organization.PostalAddress;

public class PostalAddressConverter implements Converter<Address, PostalAddress> {
    @Override
    public org.cyclonedx.model.organization.PostalAddress convert(data.Address postalAddress) {
        if (postalAddress == null) return null;

        var postalAddress1 = new org.cyclonedx.model.organization.PostalAddress();
        postalAddress1.setStreetAddress(postalAddress.streetAddress());
        postalAddress1.setLocality(postalAddress.city());
        postalAddress1.setPostalCode(postalAddress.postalCode());
        postalAddress1.setCountry(postalAddress.country());
        postalAddress1.setRegion(postalAddress.region());
        postalAddress1.setPostOfficeBoxNumber(postalAddress.postOfficeBoxNumber());
        return postalAddress1;
    }
}

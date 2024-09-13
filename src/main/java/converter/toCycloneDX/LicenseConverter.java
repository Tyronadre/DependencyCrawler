package converter.toCycloneDX;

import converter.Converter;
import data.License;

public class LicenseConverter implements Converter<License, org.cyclonedx.model.License> {
    @Override
    public org.cyclonedx.model.License convert(data.License license) {
        if (license == null) return null;

        var license1 = new org.cyclonedx.model.License();
        license1.setId(license.id());
        license1.setName(license.name());
        license1.setUrl(license.url());
        license1.setLicenseText(new AttachementTextConverter().convert(license.text()));

        return license1;
    }
}

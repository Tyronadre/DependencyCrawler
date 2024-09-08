package converter.cycloneDX;

import converter.Converter;
import data.LicenseChoice;

import java.util.List;

public class LicenseChoiceConverter implements Converter<LicenseChoice, org.cyclonedx.model.LicenseChoice> {

    @Override
    public org.cyclonedx.model.LicenseChoice convert(data.LicenseChoice licenseChoice) {
        if (licenseChoice == null) return null;

        var licenseChoice1 = new org.cyclonedx.model.LicenseChoice();
        if (licenseChoice.expression() != null)
            licenseChoice1.setExpression(new LicenseChoiceExpressionConverter().convert(licenseChoice.expression()));
        else
            licenseChoice1.setLicenses(List.of(new LicenseConverter().convert(licenseChoice.license())));

        return licenseChoice1;
    }

}

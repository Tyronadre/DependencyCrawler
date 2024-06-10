package data.internalData;

import data.License;

public class MavenLicenseChoice implements data.LicenseChoice {
    private final License license;

    public MavenLicenseChoice(License license) {
        this.license = license;
    }

    @Override
    public License getLicense() {
        return license;
    }

    @Override
    public String getExpression() {
        return null;
    }

    @Override
    public String getAcknowledgement() {
        return null;
    }
}

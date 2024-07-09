package data.internalData;

import data.License;

public class MavenLicenseChoice implements data.LicenseChoice {
    private final License license;

    public MavenLicenseChoice(License license) {
        this.license = license;
    }

    @Override
    public License license() {
        return license;
    }

    @Override
    public String expression() {
        return null;
    }

    @Override
    public String acknowledgement() {
        return null;
    }
}

package data;

import data.dataImpl.LicenseChoiceImpl;

public interface LicenseChoice {
    License getLicense();
    String getExpression();
    String getAcknowledgement();

    static LicenseChoice of(License license, String expression, String acknowledgement) {
        return new LicenseChoiceImpl(license, expression, acknowledgement);
    }
}

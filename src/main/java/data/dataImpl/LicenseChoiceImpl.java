package data.dataImpl;

import data.License;
import data.LicenseChoice;

public class LicenseChoiceImpl implements LicenseChoice {
    private final License license;
    private final String expression;
    private final String acknowledgement;

    public LicenseChoiceImpl(License license, String expression, String acknowledgement) {
        this.license = license;
        this.expression = expression;
        this.acknowledgement = acknowledgement;
    }

    @Override
    public License getLicense() {
        return license;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public String getAcknowledgement() {
        return acknowledgement;
    }
}

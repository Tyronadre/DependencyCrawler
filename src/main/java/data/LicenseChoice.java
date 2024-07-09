package data;

public interface LicenseChoice {

    License license();

    String expression();

    String acknowledgement();

    static LicenseChoice of(License license, String expression, String acknowledgement) {
        return new LicenseChoiceRecord(license, expression, acknowledgement);
    }

    record LicenseChoiceRecord(License license, String expression, String acknowledgement) implements LicenseChoice {
    }
}

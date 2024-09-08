package data;

import java.util.List;

public interface LicenseChoice {

    static LicenseChoice of(List<License> licenses, String expression, String acknowledgement) {
        return new LicenseChoiceRecord(licenses, expression, acknowledgement);
    }

    String expression();

    String acknowledgement();

    List<License> licenses();

    record LicenseChoiceRecord(List<License> licenses, String expression,
                               String acknowledgement) implements LicenseChoice {
    }
}

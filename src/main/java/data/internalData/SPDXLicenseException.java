package data.internalData;

import com.google.gson.JsonObject;
import data.LicenseException;

public class SPDXLicenseException implements LicenseException {
    JsonObject data;

    public SPDXLicenseException(JsonObject data) {
        this.data = data;
    }
}

package data.internalData;

import com.google.gson.JsonObject;
import data.LicenseException;
import data.Licensing;
import data.Property;

import java.util.List;

public class SPDXLicenseException implements LicenseException {
    JsonObject data;
    JsonObject details;

    public SPDXLicenseException(JsonObject data, JsonObject details) {
        this.data = data;
        this.details = details;
    }

    @Override
    public String id() {
        return data.get("licenseExceptionId").getAsString();
    }

    @Override
    public String name() {
        return data.get("name").getAsString();
    }

    @Override
    public String nameOrId() {
        return id();
    }

    @Override
    public String text() {
        return details.get("licenseExceptionText").getAsString();
    }

    @Override
    public String url() {
        return details.get("seeAlso").getAsString();
    }

    @Override
    public Licensing licensing() {
        return null;
    }

    @Override
    public List<Property> properties() {
        return null;
    }

    @Override
    public String acknowledgement() {
        return null;
    }

    @Override
    public String toString() {
        return "SPDXLicenseException[id=" + id() + "]";
    }
}

package data.internalData;

import com.google.gson.JsonObject;
import data.License;
import data.Licensing;
import data.Property;

import java.util.List;

public class SPDXLicense implements License {
    JsonObject data;
    JsonObject details;

    public SPDXLicense(JsonObject data, JsonObject details) {
        this.data = data;
        this.details = details;
    }

    @Override
    public String getId() {
        return data.get("licenseId").getAsString();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getText() {
        return details.get("licenseText").getAsString();
    }

    @Override
    public String getUrl() {
        return data.get("detailsUrl").getAsString();
    }

    @Override
    public Licensing getLicensing() {
        return null;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(
                Property.of("licenseId", data.get("licenseId").getAsString()),
                Property.of("seeAlso", data.get("seeAlso").getAsJsonArray().toString()),
                Property.of("isOsiApproved", data.get("isOsiApproved").getAsString()),
                Property.of("isFsfLibre", data.get("isFsfLibre").getAsString()),
                Property.of("standardLicenseTemplate", details.get("standardLicenseTemplate").getAsString()),
                Property.of("name", details.get("name").getAsString()),
                Property.of("crossRef", details.get("crossRef").getAsJsonArray().toString())
        );
    }

    @Override
    public String getAcknowledgement() {
        return null;
    }
}

package data.dataImpl;

import com.google.gson.JsonObject;
import data.License;
import data.Licensing;
import data.Property;

import java.util.List;

public class SPDXLicense implements License {
    JsonObject data;

    public SPDXLicense(JsonObject data) {
        this.data = data;
    }

    @Override
    public String getId() {
        return data.get("licenseId").getAsString();
    }

    @Override
    public String getName() {
        return data.get("name").getAsString();
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public Licensing getLicensing() {
        return null;
    }

    @Override
    public List<Property> getProperties() {
        return null;
    }

    @Override
    public String getAcknowledgement() {
        return null;
    }
}

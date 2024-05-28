package data.dataImpl;

import com.google.gson.JsonObject;
import data.License;

public class LicenseImpl implements License {
    JsonObject data;

    public LicenseImpl(JsonObject data) {
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
}

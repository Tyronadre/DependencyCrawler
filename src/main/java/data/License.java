package data;

import com.google.gson.JsonObject;
import cyclonedx.v1_6.Bom16;
import data.dataImpl.LicenseImpl;

import java.util.HashMap;

public interface License {

    String getId();

    String getName();

    HashMap<String, License> licenses = new HashMap<>();
    static License of(JsonObject data) {
        var id = data.get("licenseId").getAsString();
        if (licenses.containsKey(id)) {
            return licenses.get(id);
        }
        License license = new LicenseImpl(data);
        licenses.put(id, license);
        return license;
    }
}

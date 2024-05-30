package data;

import com.google.gson.JsonObject;
import cyclonedx.sbom.Bom16;
import data.dataImpl.SPDXLicense;

import java.util.HashMap;
import java.util.List;

public interface License {

    String getId();
    String getName();
    String getText();
    String getUrl();
    Licensing getLicensing();
    List<Property> getProperties();
    String getAcknowledgement();


    HashMap<String, License> licenses = new HashMap<>();
    static License of(JsonObject data) {
        var id = data.get("licenseId").getAsString();
        if (licenses.containsKey(id)) {
            return licenses.get(id);
        }
        License license = new SPDXLicense(data);
        licenses.put(id, license);
        return license;
    }

}

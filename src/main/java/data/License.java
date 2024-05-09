package data;

import cyclonedx.v1_6.Bom16;
import data.dataImpl.LicenseImpl;

import java.util.HashMap;

public interface License extends Bom16Component<Bom16.LicenseChoice> {


    String getName();

    String getUrl();

    String getDistribution();

    String getComments();

    String getBomRef();


    HashMap<String, License> licenses = new HashMap<>();
    static License of(String name, String url, String distribution, String comments) {
        if (licenses.containsKey(name)) {
            return licenses.get(name);
        }
        License license = new LicenseImpl(name, url, distribution, comments);
        licenses.put(name, license);
        return license;
    }
}

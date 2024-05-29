package data.dataImpl;

import data.License;

public class CustomLicense implements License {

    String name;
    String url;

    public CustomLicense(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }
}

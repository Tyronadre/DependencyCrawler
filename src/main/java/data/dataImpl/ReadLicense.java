package data.dataImpl;

import data.License;

public class ReadLicense implements License {
    private String name;
    private String url;

    public ReadLicense() {
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAcknowledgement() {
        return "";
    }

    @Override
    public void setAcknowledgement(String acknowledgement) {

    }

}

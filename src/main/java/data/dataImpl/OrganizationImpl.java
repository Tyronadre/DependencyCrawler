package data.dataImpl;

import data.Address;
import data.Organization;

public class OrganizationImpl implements Organization {
    String name;
    String url;

    public OrganizationImpl(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Address getAddress() {
        return null;
    }
}

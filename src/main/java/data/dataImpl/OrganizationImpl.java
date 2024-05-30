package data.dataImpl;

import data.Address;
import data.Organization;

public class OrganizationImpl implements Organization {
    String name;
    String url;
    Address address;

    public OrganizationImpl(String name, String url, Address address) {
        this.name = name;
        this.url = url;
        this.address = address;
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


    public Bom16.OrganizationalEntity toBom16() {
        var builder = Bom16.OrganizationalEntity.newBuilder();
        builder.setName(this.name);
        builder.addUrl(this.url);
        return builder.build();
    }
}

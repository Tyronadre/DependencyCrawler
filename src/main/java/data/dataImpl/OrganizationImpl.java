package data.dataImpl;

import cyclonedx.v1_6.Bom16;
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

    @Override
    public Bom16.OrganizationalEntity toBom16() {
        var builder = Bom16.OrganizationalEntity.newBuilder();
        builder.setName(this.name);
        builder.addUrl(this.url);
        return builder.build();
    }
}

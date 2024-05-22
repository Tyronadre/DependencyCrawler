package data.dataImpl;

import cyclonedx.v1_6.Bom16;
import data.Person;

import java.util.List;

public class PersonImpl implements Person {
    private final String name;
    private final String email;
    private final String url;
    private final String organization;
    private final String organizationUrl;
    private final List<String> roles;

    public PersonImpl(String name, String email, String url, String organization, String organizationUrl, List<String> roles) {
        this.name = name;
        this.email = email;
        this.url = url;
        this.organization = organization;
        this.organizationUrl = organizationUrl;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getOrganization() {
        return organization;
    }

    @Override
    public String getOrganizationUrl() {
        return organizationUrl;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public Bom16.OrganizationalContact toBom16() {
        return Bom16.OrganizationalContact.newBuilder().setName(getName()).setEmail(getEmail()).build();
    }
}

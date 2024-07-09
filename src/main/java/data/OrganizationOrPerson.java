package data;

public interface OrganizationOrPerson {

    Organization organization();

    Person person();

    static OrganizationOrPerson of(Organization organization) {
        return new OrganizationOrPersonRecord(organization, null);
    }

    record OrganizationOrPersonRecord(Organization organization, Person person) implements OrganizationOrPerson {
    }

}

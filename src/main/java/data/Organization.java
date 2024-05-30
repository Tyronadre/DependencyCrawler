package data;


import data.dataImpl.OrganizationImpl;

import java.util.List;

public interface Organization {
    String getName();

    List<String> getUrls();

    Address getAddress();

    List<Person> getContacts();

    static Organization of(String name, List<String> urls, Address address, List<Person> contacts) {
        return new OrganizationImpl(name, urls, address, contacts);
    }

}

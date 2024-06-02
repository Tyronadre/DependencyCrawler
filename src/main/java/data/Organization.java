package data;

import java.util.List;

public interface Organization {
    String getName();

    List<String> getUrls();

    Address getAddress();

    List<Person> getContacts();

    static Organization of(String name, List<String> urls, Address address, List<Person> contacts) {
        return new Organization() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<String> getUrls() {
                return urls;
            }

            @Override
            public Address getAddress() {
                return address;
            }

            @Override
            public List<Person> getContacts() {
                return contacts;
            }
        };
    }

}

package data;

import java.util.List;

public interface Organization {

    String name();

    List<String> urls();

    Address address();

    List<Person> contacts();

    static Organization of(String name, List<String> urls, Address address, List<Person> contacts) {
        return new OrganizationRecord(name, urls, address, contacts);
    }

    record OrganizationRecord(String name, List<String> urls, Address address, List<Person> contacts) implements Organization {
    }

}

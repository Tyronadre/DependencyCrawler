package data;

import java.util.List;

public interface Person {

    String name();

    String email();

    String url();

    String phone();

    Organization organization();

    List<String> roles();

    static Person of(String name, String email, String url, String phone, Organization organization, List<String> roles) {
        return new PersonRecord(name, email, url, phone, organization, roles);
    }

    record PersonRecord(String name, String email, String url, String phone, Organization organization, List<String> roles) implements Person {
    }

}

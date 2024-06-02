package data;

import java.util.List;

public interface Person {
    String getName();
    String getEmail();
    String getUrl();
    String getPhone();
    Organization getOrganization();
    List<String> getRoles();

    static Person of(String name, String email, String url, String phone, Organization organization,  List<String> roles) {
        return new Person() {
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
            public String getPhone() {
                return phone;
            }

            @Override
            public Organization getOrganization() {
                return organization;
            }

            @Override
            public List<String> getRoles() {
                return roles;
            }
        };
    }

}

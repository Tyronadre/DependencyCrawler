package data;

import cyclonedx.v1_6.Bom16;
import data.dataImpl.PersonImpl;

import java.util.List;

public interface Person extends Bom16Component<Bom16.OrganizationalContact> {
    String getName();
    String getEmail();
    String getUrl();
    String getPhone();
    Organization getOrganization();
    List<String> getRoles();

    static Person of(String name, String email, String url, String phone, Organization organization,  List<String> roles) {
        return new PersonImpl(name, email, url, organization, roles);
    }

}

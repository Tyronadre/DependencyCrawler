package data;

import cyclonedx.v1_6.Bom16;

public interface Person extends Bom16Component<Bom16.OrganizationalContact> {
    String getName();
    String getEmail();
    String getUrl();
}

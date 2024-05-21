package data;

import cyclonedx.v1_6.Bom16;

public interface Organization extends Bom16Component<Bom16.OrganizationalEntity> {
    String getName();
    String getUrl();
    Address getAddress();
}

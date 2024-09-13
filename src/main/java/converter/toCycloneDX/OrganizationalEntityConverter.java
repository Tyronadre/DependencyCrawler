package converter.toCycloneDX;

import converter.Converter;
import data.Organization;
import org.cyclonedx.model.OrganizationalEntity;

public class OrganizationalEntityConverter implements Converter<data.Organization, org.cyclonedx.model.OrganizationalEntity> {
    @Override
    public OrganizationalEntity convert(Organization organization) {
        if (organization == null) return null;

        var organizationalEntity = new OrganizationalEntity();
        organizationalEntity.setName(organization.name());
        organizationalEntity.setUrls(organization.urls());
        organizationalEntity.setAddress(new PostalAddressConverter().convert(organization.address()));
        organizationalEntity.setContacts(new OrganizationalContactConverter().convertList(organization.contacts()));
        return organizationalEntity;
    }
}

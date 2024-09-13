package converter.toCycloneDX;

import converter.Converter;

public class OrganizationalContactConverter implements Converter<data.Person, org.cyclonedx.model.OrganizationalContact> {
    @Override
    public org.cyclonedx.model.OrganizationalContact convert(data.Person person) {
        if (person == null) return null;

        var person1 = new org.cyclonedx.model.OrganizationalContact();
        person1.setName(person.name());
        person1.setEmail(person.email());
        person1.setPhone(person.phone());
        return person1;
    }
}

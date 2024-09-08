package converter.cycloneDX;

import converter.Converter;
import data.Property;

public class PropertyConverter implements Converter<data.Property, org.cyclonedx.model.Property> {
    @Override
    public org.cyclonedx.model.Property convert(Property property) {
        if (property == null) return null;

        var property1 = new org.cyclonedx.model.Property();
        property1.setName(property.name());
        property1.setValue(property.value());
        return property1;
    }
}

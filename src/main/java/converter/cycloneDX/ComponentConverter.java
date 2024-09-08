package converter.cycloneDX;

import converter.Converter;
import org.cyclonedx.model.Component;

public class ComponentConverter implements Converter<data.Component, org.cyclonedx.model.Component> {

    @Override
    public Component convert(data.Component component) {
        if (component == null) return null;

        var cycloneDxComponent = new Component();
        cycloneDxComponent.setSupplier(new OrganizationalEntityConverter().convert(component.getSupplier()));
        cycloneDxComponent.setPublisher(component.getPublisher());
        cycloneDxComponent.setGroup(component.getGroup());
        cycloneDxComponent.setName(component.getArtifactId());
        cycloneDxComponent.setVersion(component.getVersion().version());
        cycloneDxComponent.setDescription(component.getDescription());
        cycloneDxComponent.setHashes(new HashConverter().convertList(component.getAllHashes()));
        cycloneDxComponent.setLicenses(new LicenseChoiceConverter().convertList(component.getAllLicenses()).stream().findFirst().orElse(null));
        cycloneDxComponent.setPurl(component.getPurl());
        cycloneDxComponent.setExternalReferences(new ExternalReferenceConverter().convertList(component.getAllExternalReferences()));
        cycloneDxComponent.setProperties(new PropertyConverter().convertList(component.getAllProperties()));
        cycloneDxComponent.setManufacturer(new OrganizationalEntityConverter().convert(component.getManufacturer()));
        cycloneDxComponent.setAuthors(new OrganizationalContactConverter().convertList(component.getAllAuthors()));
        cycloneDxComponent.setType(getType(component));
        cycloneDxComponent.setBomRef(component.getPurl());

        return cycloneDxComponent;
    }

    private Component.Type getType(data.Component component) {
        if (component.getRepository() != null) {
            return Component.Type.LIBRARY;
        }
        return Component.Type.APPLICATION;
    }
}

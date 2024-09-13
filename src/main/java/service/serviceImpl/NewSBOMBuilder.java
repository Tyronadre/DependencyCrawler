package service.serviceImpl;

import converter.toCycloneDX.ComponentConverter;
import converter.toCycloneDX.ScopeConverter;
import data.Component;
import org.cyclonedx.Version;
import org.cyclonedx.exception.GeneratorException;
import org.cyclonedx.generators.json.BomJsonGenerator;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.OrganizationalEntity;
import org.cyclonedx.model.organization.PostalAddress;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NewSBOMBuilder implements DocumentBuilder<Component, Bom> {

    List<Integer> buildComponents = new ArrayList<>();

    HashMap<String, Dependency> buildDependencies = new HashMap<>();

    @Override
    public void buildDocument(Component data, String outputFileName) {

        var bom = new Bom();
        bom.setVersion(1);
        bom.setSerialNumber(UUID.randomUUID().toString());
        bom.setMetadata(buildMetadata(data));

        //build the root component
        bom.addComponent(new ComponentConverter().convert(data));
        data.getDependenciesFlatFiltered().forEach(
                dependency -> buildComponentAndDependency(dependency, bom)
        );

        var file = new File(outputFileName + ".sbom.json");

        if (!file.getAbsoluteFile().getParentFile().exists()) {
            //create out dir if not exists
            var outDir = file.getParentFile();
            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    return;
                }
            }
        }

        var generator = new BomJsonGenerator(bom, Version.VERSION_16);

        try (var writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(generator.toJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneratorException e) {
            logger.error("Failed to generate SBOM", e);
        }
    }

    private void buildComponentAndDependency(data.Dependency dependency, Bom bom) {
        var component = dependency.getComponent();
        if (component == null) return;
        if (!component.isLoaded()) return;
        if (buildComponents.contains(component.hashCode())) return;
        buildComponents.add(component.hashCode());

        var sbomComponent = new ComponentConverter().convert(component);
        sbomComponent.setScope(new ScopeConverter().convert(dependency.getScope()));
        if (!dependency.isNotOptional()) {
            sbomComponent.setScope(org.cyclonedx.model.Component.Scope.OPTIONAL);
        }
        bom.addComponent(sbomComponent);


        buildDependencies.putIfAbsent(dependency.getTreeParent().getPurl(), new Dependency(dependency.getTreeParent().getPurl()));
        var parentSbomDependency = buildDependencies.get(dependency.getTreeParent().getPurl());
        buildDependencies.putIfAbsent(component.getPurl(), new Dependency(component.getPurl()));
        parentSbomDependency.addDependency(buildDependencies.get(component.getPurl()));
        bom.addDependency(parentSbomDependency);

    }


    private Metadata buildMetadata(Component rootComponent) {
        var contact = new OrganizationalContact();
        contact.setName("Henrik Bornemann");
        contact.setEmail("henrik.bornemann@stud.tu-darmstadt.de");

        var address = new PostalAddress();
        address.setStreetAddress("Hochschulstrasse 10");
        address.setPostalCode("64289");
        address.setLocality("Darmstadt");
        address.setCountry("Germany");

        var supplier = new OrganizationalEntity();
        supplier.setName("Technische Universitaet Darmstadt");
        supplier.setAddress(address);
        supplier.setContacts(List.of(contact));
        supplier.setUrls(List.of("https://www.tu-darmstadt.de"));

        var metadata = new Metadata();
        metadata.setTimestamp(new Date());
        metadata.setSupplier(supplier);
        metadata.setManufacturer(supplier);
        metadata.setComponent(new ComponentConverter().convert(rootComponent));

        return metadata;
    }
}

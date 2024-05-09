package service.serviceImpl;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import cyclonedx.v1_6.Bom16;
import data.Component;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import data.License;
import data.Organization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SBOMBuilder {
    private final HashMap<Component, Bom16.Component.Builder> componentToComponentBuilder = new HashMap<>();

    public void createSBOM(Component root, String outputFileName) {
        componentToComponentBuilder.clear();

        createComponentBuilders(root);

        var bom = buildBom(root);

        var outputFileDir = outputFileName.split("/",2);
        if (outputFileDir.length > 1) {
            //create out dir if not exists
            File outDir = new File(outputFileDir[0]);
            if (!outDir.exists()) {
                outDir.mkdir();
            }
        }

        // tree
        root.printTree( outputFileName + ".tree");

        // serialize to file
//        try {
//            var file = new File("out/" + outputFileName + ".dat");
//            var outputStream = CodedOutputStream.newInstance(new FileOutputStream(file));
//            bom.writeTo(outputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // json file
        try {
            var file = new File(outputFileName + ".json");
            var outputStream = new FileWriter(file);
            outputStream.write(JsonFormat.printer().print(bom));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createComponentBuilders(Component root) {
        createComponentBuilder(root, true);

        for (var dependency : root.getDependencies()) {
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded()) {
                createComponentBuilder(dependency.getComponent(), false);
            }
        }
    }

    private void createComponentBuilder(Component component, boolean isRoot) {
        var componentBuilder = Bom16.Component.newBuilder();

        if (isRoot) componentBuilder.setType(Bom16.Classification.CLASSIFICATION_APPLICATION);
        else componentBuilder.setType(Bom16.Classification.CLASSIFICATION_LIBRARY);

        componentBuilder.setBomRef(component.getQualifiedName());
        if (component.getSupplier() != null) {
            componentBuilder.setSupplier(buildSupplier(component.getSupplier()));
            componentBuilder.setPublisher(component.getSupplier().toString());
        }
        componentBuilder.setGroup(component.getGroup());
        componentBuilder.setName(component.getName());
        componentBuilder.setVersion(component.getVersion().getVersion());
        Optional.ofNullable(component.getDescription()).ifPresent(componentBuilder::setDescription);
        Optional.ofNullable(component.getManufacturer()).ifPresent(v -> componentBuilder.setPublisher(v.getName()));
        componentBuilder.addAllHashes(buildAllHashes(component));
        componentBuilder.addAllLicenses(buildAllLicences(component));
        componentBuilder.setPurl(component.getPurl());
        componentBuilder.addAllExternalReferences(buildAllExternalReferences(component));
//        componentBuilder.setSwid(component.getSwid());

        componentToComponentBuilder.put(component, componentBuilder);

        for (var dependency : component.getDependencies()) {
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded()) {
                createComponentBuilder(dependency.getComponent(), false);
            }
        }
    }

    private Iterable<Bom16.Hash> buildAllHashes(Component component) {
        return component.getAllHashes().stream().map(Hash::toBom16).toList();
    }

    private List<Bom16.ExternalReference> buildAllExternalReferences(Component component) {
        return component.getAllExternalReferences().stream().map(ExternalReference::toBom16).toList();
    }

    private List<? extends Bom16.LicenseChoice> buildAllLicences(Component component) {
        return component.getAllLicences().stream().map(License::toBom16).toList();
    }

    private Bom16.Bom buildBom(Component root) {
        var bomBuilder = Bom16.Bom.newBuilder();

        bomBuilder.setSpecVersion("1.6");
        bomBuilder.setVersion(1);
        bomBuilder.setSerialNumber(UUID.randomUUID().toString());
        bomBuilder.setMetadata(buildMetadata(root));
        bomBuilder.addAllServices(buildServices(root));
        bomBuilder.addAllExternalReferences(buildExternalReferences(root));
        bomBuilder.addAllDependencies(buildDependencies(root));
        bomBuilder.addAllComponents(buildComponents(root));
        bomBuilder.addAllCompositions(buildCompositions(root));
        bomBuilder.addAllVulnerabilities(buildVulnerabilities(root));
        bomBuilder.addAllAnnotations(buildAnnotations(root));
        bomBuilder.addAllProperties(buildProperties(root));
        bomBuilder.addAllFormulation(buildFormulation(root));
        bomBuilder.addAllDeclarations(buildDeclarations(root));
        bomBuilder.addAllDefinitions(buildDefinitions(root));

        return bomBuilder.build();
    }

    private Bom16.Metadata buildMetadata(Component root) {
        var metadataBuilder = Bom16.Metadata.newBuilder();

        long millis = System.currentTimeMillis();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000).setNanos((int) ((millis % 1000) * 1000000)).build();
        metadataBuilder.setTimestamp(timestamp);
        metadataBuilder.setComponent(buildComponent(root));
        metadataBuilder.setManufacturer(buildManufacturer());

        return metadataBuilder.build();
    }

    /**
     * @return the manufacturer of the sbom (us).
     */
    private Bom16.OrganizationalEntity buildManufacturer() {
        var manufacturerBuilder = Bom16.OrganizationalEntity.newBuilder();
        manufacturerBuilder.setName("Technische Universität Darmstadt");
        return manufacturerBuilder.build();
    }

    private Bom16.Component buildComponent(Component component) {
        return componentToComponentBuilder.get(component).build();
    }

    private Bom16.Scope buildScope(Dependency dependency) {
        return switch (dependency.getScope()) {
            case "provided", "test", "runtime" -> Bom16.Scope.SCOPE_EXCLUDED;
            default -> Bom16.Scope.SCOPE_REQUIRED;
        };
    }

    private Bom16.OrganizationalEntity buildSupplier(Organization supplier) {
        return null;
    }

    /**
     * Recursively build all components of the sbom.
     *
     * @param component the component to build
     * @return a list of all components of the sbom
     */
    private List<Bom16.Component> buildComponents(Component component) {
        List<Bom16.Component> components = new ArrayList<>();
        for (var componentBuilder : componentToComponentBuilder.values()) {
            components.add(componentBuilder.build());
        }
        return components;
    }

    private List<Bom16.Service> buildServices(Component component) {
        List<Bom16.Service> services = new ArrayList<>();

        return services;
    }

    private List<Bom16.ExternalReference> buildExternalReferences(Component component) {
        List<Bom16.ExternalReference> externalReferences = new ArrayList<>();

        return externalReferences;
    }

    private List<Bom16.Dependency> buildDependencies(Component component) {
        List<Bom16.Dependency> dependencies = new ArrayList<>();

        for (var dependency : component.getDependencies()) {
            var componentBuilder = componentToComponentBuilder.get(dependency.getComponent());
            if (componentBuilder == null) continue;
            componentBuilder.setScope(buildScope(dependency));
            dependencies.add(buildDependency(componentBuilder.getBomRef(), dependency));
        }

        return dependencies;
    }

    private Bom16.Dependency buildDependency(String bomRef, Dependency dependency) {
        var dependencyBuilder = Bom16.Dependency.newBuilder();
        dependencyBuilder.setRef(bomRef);
        dependencyBuilder.addAllDependencies(buildDependencies(dependency.getComponent()));

        return dependencyBuilder.build();
    }

    private List<Bom16.Composition> buildCompositions(Component component) {
        List<Bom16.Composition> compositions = new ArrayList<>();

        return compositions;
    }

    private List<Bom16.Vulnerability> buildVulnerabilities(Component component) {
        List<Bom16.Vulnerability> vulnerabilities = new ArrayList<>();

        return vulnerabilities;
    }

    private List<Bom16.Annotation> buildAnnotations(Component component) {
        List<Bom16.Annotation> annotations = new ArrayList<>();

        return annotations;
    }

    private List<Bom16.Property> buildProperties(Component component) {
        List<Bom16.Property> properties = new ArrayList<>();

        return properties;
    }

    private List<Bom16.Formula> buildFormulation(Component component) {
        List<Bom16.Formula> formulations = new ArrayList<>();

        return formulations;
    }

    private List<Bom16.Declarations> buildDeclarations(Component component) {
        List<Bom16.Declarations> declarations = new ArrayList<>();

        return declarations;
    }

    private List<Bom16.Definition> buildDefinitions(Component component) {
        List<Bom16.Definition> definitions = new ArrayList<>();

        return definitions;
    }


}

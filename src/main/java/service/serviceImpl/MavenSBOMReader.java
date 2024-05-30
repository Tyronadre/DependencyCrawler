package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.sbom.Bom16;
import data.Component;
import data.Metadata;
import data.dataImpl.MetadataImpl;
import logger.Logger;
import repository.LicenseRepository;
import service.DocumentReader;
import util.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import static service.converter.BomToInternalMavenConverter.*;

public class MavenSBOMReader implements DocumentReader {
    private static final LicenseRepository licenseRepository = LicenseRepository.getInstance();

    @Override
    public Component readDocument(String inputFileName) {
        Logger logger = Logger.of(MavenSBOMReader.class.getName());

        //parse from file
        var builder = Bom16.Bom.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(new File(inputFileName).toPath(), StandardCharsets.UTF_8), builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        var bom = builder.build();

        //check format
        if (!bom.getBomFormat().equals("CycloneDX")) {
            throw new IllegalArgumentException("Unsupported SBOM format: " + bom.getBomFormat());
        }
        if (!bom.getSpecVersion().equals("1.6")) {
            logger.error("Unsupported SBOM version: " + bom.getSpecVersion() + ". Try parsing anyway. May result in errors/missing data. Supported version: 1.6");
        }

        var bomComponents = new HashMap<String, Pair<Bom16.Component, Component>>();
        //build root
        var bomRoot = bom.getMetadata().getComponent();
        var root = buildComponent(bomRoot);
        root.setRoot();
        bomComponents.put(bomRoot.getBomRef(), new Pair<>(bom.getMetadata().getComponent(), root));

        //build all components in the SBOM
        bom.getComponentsList().forEach(bomComponent -> bomComponents.put(bomComponent.getBomRef(), new Pair<>(bomComponent, buildComponent(bomComponent))));

        //build dependencies from the SBOM
        buildAllDependencies(bom.getDependenciesList(), bomComponents);

        //build vulnerabilities from the SBOM
        buildAllVulnerabilities(bom.getVulnerabilitiesList(), bomComponents);

        return root;
    }

//    private Metadata buildMetadata(Bom16.Metadata bomMetadata) {
//        var metadataImpl = new MetadataImpl();
//        if (bomMetadata.hasTimestamp()) metadataImpl.setProperty("timestamp", bomMetadata.getTimestamp().toString());
//        if (bomMetadata.hasTools()) metadataImpl.setProperty("tools", bomMetadata.getTools());
//        if (bomMetadata.getAuthorsCount() > 0)
//            metadataImpl.setProperty("authors", buildAllPersons(bomMetadata.getAuthorsList(), null));
//        if (bomMetadata.hasComponent()) metadataImpl.setProperty("component", buildRoot(bomMetadata.getComponent()));
//        if (bomMetadata.hasSupplier())
//            metadataImpl.setProperty("supplier", buildOrganization(bomMetadata.getSupplier()));
//        if (bomMetadata.getLicensesCount() > 0)
//            metadataImpl.setProperty("licenses", buildAllLicenseChoices(bomMetadata.getLicensesList()));
//        if (bomMetadata.getPropertiesCount() > 0)
//            metadataImpl.setProperty("properties", buildAllProperties(bomMetadata.getPropertiesList()));
//        if (bomMetadata.hasManufacturer())
//            metadataImpl.setProperty("manufacturer", buildOrganization(bomMetadata.getManufacturer()));
//        return metadataImpl;
//    }
}


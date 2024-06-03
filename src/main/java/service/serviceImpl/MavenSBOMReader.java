package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.sbom.Bom16;
import data.Component;
import logger.Logger;
import repository.LicenseRepository;
import service.DocumentReader;
import service.converter.BomToInternalMavenConverter;
import util.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.HashMap;

import static service.converter.BomToInternalMavenConverter.*;

public class MavenSBOMReader implements DocumentReader {
    private static final Logger logger = Logger.of("MavenSBOMReader");

    private static final LicenseRepository licenseRepository = LicenseRepository.getInstance();

    @Override
    public Component readDocument(String inputFileName) {
        logger.info("Reading document as SBOM: " + inputFileName);

        var file = new File(inputFileName);

        //parse from file
        var builder = Bom16.Bom.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(file.toPath(), StandardCharsets.UTF_8), builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        var bom = builder.build();

        logger.info("Read from file. Parsing... ");

        //check format
        if (!bom.getBomFormat().equals("CycloneDX")) {
            throw new IllegalArgumentException("Unsupported SBOM format: " + bom.getBomFormat());
        }
        if (!bom.getSpecVersion().equals("1.6")) {
            logger.error("Unsupported SBOM version: " + bom.getSpecVersion() + ". Try parsing anyway. May result in errors/missing data. Supported version: 1.6");
        }

        //build root
        var bomRoot = bom.getMetadata().getComponent();
        var root = buildComponent(bomRoot);
        root.setRoot();

        logger.info("building components");
        //build all components in the SBOM
        bom.getComponentsList().forEach(BomToInternalMavenConverter::buildComponent);

        logger.info("building dependencies");
        //build dependencies from the SBOM
        buildAllDependenciesRecursively(bom.getDependencies(0), null);


        logger.info("parsing vulnerabilities");
        //build vulnerabilities from the SBOM
        buildAllVulnerabilities(bom.getVulnerabilitiesList());

        logger.success("Parsed from SBOM File: " + file.getAbsolutePath());

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


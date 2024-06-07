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

import static service.converter.BomToInternalMavenConverter.*;

public class MavenSBOMReader implements DocumentReader<Bom16.Bom> {
    private static final Logger logger = Logger.of("MavenSBOMReader");

    private static final LicenseRepository licenseRepository = LicenseRepository.getInstance();

    @Override
    public Pair<Bom16.Bom, Component> readDocument(String inputFileName) {
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

        return new Pair<>(bom, root);
    }
}


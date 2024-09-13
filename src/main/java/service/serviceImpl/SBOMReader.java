package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.sbom.Bom16;
import data.Component;
import data.readData.ReadDependency;
import data.readData.ReadSBomVulnerability;
import dependencyCrawler.DependencyCrawlerInput;
import logger.Logger;
import repository.repositoryImpl.ReadComponentRepository;
import service.DocumentReader;
import util.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;


public class SBOMReader implements DocumentReader<Pair<Bom16.Bom, Component>> {
    private static final Logger logger = Logger.of("MavenSBOMReader");


    @Override
    public Pair<Bom16.Bom, Component> readDocument(String inputFileName) {
        logger.info("Reading document as SBOM: " + inputFileName);

        var file = new File(inputFileName);


        //parse from file
        var builder = Bom16.Bom.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(file.toPath(), StandardCharsets.UTF_8), builder);
        } catch (Exception e) {
            logger.error("Could not read from file: " + file.getAbsolutePath() + ". Cause: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
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

        //build all components in the SBOM
        logger.info("building components");
        bom.getComponentsList().forEach(this::buildComponent);

        //build root
        logger.info("building root");
        var bomRoot = bom.getMetadata().getComponent();
        var root = buildComponent(bomRoot);
        root.setRoot();

        logger.info("building dependencies");
        //build dependencies from the SBOM
        buildDependencies(bom.getDependenciesList());

        logger.info("parsing vulnerabilities");
        //build vulnerabilities from the SBOM
        buildAllVulnerabilities(bom.getVulnerabilitiesList());

        logger.success("Parsed from SBOM File: " + file.getAbsolutePath());

        return new Pair<>(bom, root);
    }

    private void buildDependencies(List<Bom16.Dependency> dependenciesList) {
        var dependenciesToProcess = new ArrayDeque<>(dependenciesList);

        while (!dependenciesToProcess.isEmpty()) {
            var sbomDependency = dependenciesToProcess.poll();
            var parent = buildComponents.get(sbomDependency.getRef());
            if (parent == null) {
                logger.error("Parent component not found for dependency: " + sbomDependency.getRef());
                continue;
            }

            for (var child : sbomDependency.getDependenciesList()) {
                var childComponent = buildComponents.get(child.getRef());
                if (childComponent == null) {
                    logger.error("Child component not found for dependency: " + sbomDependency.getRef() + " -> " + child.getRef());
                    continue;
                }
                parent.addDependency(new ReadDependency(childComponent, parent));

                dependenciesToProcess.addAll(child.getDependenciesList());
            }
        }
    }

    public void buildAllVulnerabilities(List<Bom16.Vulnerability> bomVulnerabilities) {
        for (var bomVulnerability : bomVulnerabilities) {
            var componentRef = bomVulnerability.getPropertiesList().stream().filter(it -> it.getName().equals("componentRef")).findFirst();
            if (componentRef.isEmpty()) {
                continue;
            }
            var component = ReadComponentRepository.getInstance().getReadComponent(componentRef.get().getValue());
            new ReadSBomVulnerability(bomVulnerability, component);
        }
    }

    HashMap<String, Component> buildComponents = new HashMap<>();
    public Component buildComponent(Bom16.Component bomComponent) {
        var purl = bomComponent.getPurl();
        DependencyCrawlerInput.Type type;
        if (purl.isEmpty()) {
            type = DependencyCrawlerInput.Type.OTHER;
            logger.info("Component has no purl: " + bomComponent.getBomRef());
        } else if (purl.contains("maven")) {
            type = DependencyCrawlerInput.Type.MAVEN;
        } else if (purl.contains("conan")) {
            type = DependencyCrawlerInput.Type.CONAN;
        } else if (purl.contains("android_native")) {
            type = DependencyCrawlerInput.Type.ANDROID_NATIVE;
        } else if (purl.contains("jitpack")) {
            type = DependencyCrawlerInput.Type.JITPACK;
        } else {
            type = DependencyCrawlerInput.Type.OTHER;
            logger.error("Unknown type in purl. Skipping: " + purl + ". Supported types are: pkg:maven, pkg:conan, pkg:android_native, pkg:jitpack.");
        }
        var newComp = ReadComponentRepository.getInstance().getSBomComponent(bomComponent, type, purl);
        buildComponents.put(bomComponent.getBomRef(), newComp);
        return newComp;
    }
}


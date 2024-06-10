package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.sbom.Bom16;
import data.*;
import repository.repositoryImpl.ReadVulnerabilityRepository;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static service.converter.InternalMavenToBomConverter.*;

public class MavenSBOMBuilder implements DocumentBuilder<Bom16.Bom> {
    private final HashMap<Component, Bom16.Component.Builder> componentToComponentBuilder = new HashMap<>();

    @Override
    public void buildDocument(Component root, String outputFileName) {
        var start = System.currentTimeMillis();
        componentToComponentBuilder.clear();

        logger.info("Creating SBOM for " + root.getQualifiedName() + "...");

        var bom = buildBom(root);

        logger.info("SBOM created. Writing to file...");

        var outputFileDir = outputFileName.split("/", 2);
        if (outputFileDir.length > 1) {
            //create out dir if not exists
            File outDir = new File(outputFileDir[0]);
            if (!outDir.exists()) {
                outDir.mkdir();
            }
        }

        // json file
        try {
            var file = new File(outputFileName + ".sbom.json");
            var outputStream = new FileWriter(file, StandardCharsets.UTF_8);
            outputStream.write(JsonFormat.printer().print(bom));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON.");
        }

        logger.success(new File(outputFileName).getAbsolutePath() + ".sbom.json saved (" + (System.currentTimeMillis() - start) + "ms)");
    }

    @Override
    public void rebuildDocument(Bom16.Bom bom, String path) {
        bom = updateBom(bom);
        try {
            var file = new File(path);
            var outputStream = new FileWriter(file + ".sbom.json", StandardCharsets.UTF_8);
            outputStream.write(JsonFormat.printer().print(bom));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON.");
        }
    }

    private Bom16.Bom buildBom(Component root) {
        var bomBuilder = Bom16.Bom.newBuilder();

        var components = new HashMap<String, Bom16.Component>();
        var dependency = buildAllDependenciesAndComponentsRecursively(root, components);

        bomBuilder.setBomFormat("CycloneDX");
        bomBuilder.setSpecVersion("1.6");
        bomBuilder.setVersion(1);
        bomBuilder.setSerialNumber(UUID.randomUUID().toString());
        bomBuilder.setMetadata(buildMetadata(root));
        bomBuilder.addDependencies(dependency);
        bomBuilder.addAllComponents(components.values().stream().sorted(Comparator.comparing(Bom16.Component::getBomRef)).toList());
        var vuls = root.getDependencyComponentsFlatFiltered().stream().map(Component::getAllVulnerabilities).flatMap(Collection::stream).collect(Collectors.toSet());
        vuls.addAll(ReadVulnerabilityRepository.getInstance().getAllVulnerabilities());
        bomBuilder.addAllVulnerabilities(buildAllVulnerabilities(vuls));

        return bomBuilder.build();
    }

    private Bom16.Bom updateBom(Bom16.Bom bom) {
        var bomBuilder = Bom16.Bom.newBuilder(bom);

        bomBuilder.setVersion(bom.getVersion() + 1);

        bomBuilder.setMetadata(bom.getMetadata().toBuilder().setTimestamp(buildTimestamp(Timestamp.of(System.currentTimeMillis()/1000,0))));

        return bomBuilder.build();
    }

}



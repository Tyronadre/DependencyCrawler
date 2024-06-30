package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.sbom.Bom16;
import data.Component;
import data.Timestamp;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static service.converter.InternalMavenToBomConverter.buildAllDependenciesAndComponentsRecursively;
import static service.converter.InternalMavenToBomConverter.buildAllVulnerabilities;
import static service.converter.InternalMavenToBomConverter.buildMetadata;
import static service.converter.InternalMavenToBomConverter.buildTimestamp;

public class SBOMBuilder implements DocumentBuilder<Component, Bom16.Bom> {

    @Override
    public void buildDocument(Component root, String outputFileName) {
        var start = System.currentTimeMillis();

        logger.info("Creating SBOM for " + root.getQualifiedName() + "...");

        var bom = buildBom(root);

        logger.info("SBOM created. Writing to file...");

        var file = new File(outputFileName + ".sbom.json");

        if (!file.getParentFile().exists()) {
            //create out dir if not exists
            var outDir = file.getParentFile();
            if (!outDir.exists()) {
                logger.info("Creating output directory. " + outDir.getAbsolutePath());
                if (!outDir.mkdirs()) {
                    logger.error("Failed to create output directory.");
                    return;
                }
            }
        }

        // json file
        try {
            var outputStream = new FileWriter(file, StandardCharsets.UTF_8);
            outputStream.write(JsonFormat.printer().print(bom));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON while building SBOM. " + new File(outputFileName + ".sbom.json").getAbsolutePath(), e);
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
            logger.error("Failed writing to JSON while rebuild SBOM." + new File(path + ".sbom.json").getAbsolutePath(), e);
        }
    }

    private Bom16.Bom buildBom(Component root) {
        var bomBuilder = Bom16.Bom.newBuilder();

        var components = new HashMap<String, Bom16.Component>();
        var dependencies = buildAllDependenciesAndComponentsRecursively(root, components);

        bomBuilder.setBomFormat("CycloneDX");
        bomBuilder.setSpecVersion("1.6");
        bomBuilder.setVersion(1);
        bomBuilder.setSerialNumber(UUID.randomUUID().toString());
        bomBuilder.setMetadata(buildMetadata(root));
        bomBuilder.addAllDependencies(dependencies);
        bomBuilder.addAllComponents(components.values().stream().filter(it -> !Objects.equals(it.getPurl(), root.getPurl())).sorted(Comparator.comparing(Bom16.Component::getBomRef)).toList());
        var vuls = root.getDependencyComponentsFlatFiltered().stream().map(Component::getAllVulnerabilities).flatMap(Collection::stream).collect(Collectors.toSet());
        bomBuilder.addAllVulnerabilities(buildAllVulnerabilities(vuls));

        return bomBuilder.build();
    }

    private Bom16.Bom updateBom(Bom16.Bom bom) {
        var bomBuilder = Bom16.Bom.newBuilder(bom);

        bomBuilder.setVersion(bom.getVersion() + 1);

        bomBuilder.setMetadata(bom.getMetadata().toBuilder().setTimestamp(buildTimestamp(Timestamp.of(System.currentTimeMillis() / 1000, 0))));

        return bomBuilder.build();
    }

}



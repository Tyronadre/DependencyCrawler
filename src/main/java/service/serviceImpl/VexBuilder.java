package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.vex.VexOuterClass;
import data.Component;
import data.Vulnerability;
import data.VulnerabilityRating;
import data.VulnerabilityReference;
import logger.Logger;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class VexBuilder implements DocumentBuilder<Component, Iterable<Vulnerability>> {
    private static final Logger logger = Logger.of("VexBuilder");

    @Override
    public void buildDocument(Component root, String outputFileName) {
        var start = System.currentTimeMillis();

        logger.info("Creating VEX for " + root.getQualifiedName() + "...");


        var vex = buildVex(root);

        if (vex.getVulnerabilitiesCount() == 0) {
            logger.success("No vulnerabilities found. No VEX will be created.");
            return;
        }

        logger.info("VEX created. Writing to file...");

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
            var file = new File(outputFileName + ".vex.json");
            var outputStream = new FileWriter(file);
            outputStream.write(JsonFormat.printer().print(vex));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON.", e);
        }

        logger.success(new File(outputFileName).getAbsolutePath() + ".vex.json saved (" + (System.currentTimeMillis() - start) + "ms)");
    }

    private VexOuterClass.Vex buildVex(Component root) {
        var builder = VexOuterClass.Vex.newBuilder();
        builder.addAllVulnerabilities(buildAllVulnerabilities(root));
        return builder.build();
    }

    private Iterable<VexOuterClass.Vulnerability> buildAllVulnerabilities(Component root) {
        var list = new ArrayList<VexOuterClass.Vulnerability>();

        for (var component : root.getDependencyComponentsFlatFiltered().stream().sorted(Comparator.comparing(Component::getQualifiedName)).toList()) {
            var vulnerabilities = component.getAllVulnerabilities();
            if (vulnerabilities != null) {
                for (var vulnerability : vulnerabilities) {
                    list.add(buildVulnerability(vulnerability));
                }
            }
        }

        return list;
    }

    private VexOuterClass.Vulnerability buildVulnerability(Vulnerability vulnerability) {
        var builder = VexOuterClass.Vulnerability.newBuilder();
        builder.setId(vulnerability.getId());
        builder.setRef(vulnerability.getComponent().getQualifiedName());
        Optional.ofNullable(vulnerability.getAllReferences()).flatMap(refs -> refs.stream().filter(ref -> ref.type().equals("WEB")).findFirst()).ifPresent(ref -> builder.setSource(buildSource(ref)));
        Optional.ofNullable(vulnerability.getAllRatings()).ifPresent(severities -> builder.addAllRatings(buildAllRatings(severities)));
        Optional.ofNullable(vulnerability.getAllCwes()).ifPresent(cwes -> builder.addAllCwes(buildAllCwes(cwes)));
        Optional.ofNullable(vulnerability.getDescription()).ifPresent(builder::setDescription);
        Optional.ofNullable(vulnerability.getAllRecommendations()).ifPresent(builder::addAllRecommendations);
        Optional.ofNullable(vulnerability.getAllReferences().stream().filter(ref -> ref.type().equals("ADVISORY"))).ifPresent(refs -> builder.addAllAdvisories(buildAllAdvisories(refs)));
        return builder.build();
    }

    private Iterable<String> buildAllAdvisories(Stream<VulnerabilityReference> refs) {
        var list = new ArrayList<String>();
        refs.forEach(ref -> list.add(ref.source().value()));
        return list;
    }

    private Iterable<VexOuterClass.Cwe> buildAllCwes(List<Integer> cwes) {
        var list = new ArrayList<VexOuterClass.Cwe>();
        for (var cwe : cwes) {
            var builder = VexOuterClass.Cwe.newBuilder();
            builder.setCwe(cwe);
            list.add(builder.build());
        }
        return list;
    }

    private Iterable<VexOuterClass.Rating> buildAllRatings(List<VulnerabilityRating> vulnerabilitySeverities) {
        var list = new ArrayList<VexOuterClass.Rating>();
        for (var severity : vulnerabilitySeverities) {
            var builder = VexOuterClass.Rating.newBuilder();
            builder.setScore(buildScore(severity));
            Optional.ofNullable(buildSeverity(severity)).ifPresent(builder::setSeverity);
            Optional.ofNullable(buildMethod(severity)).ifPresent(builder::setMethod);
            builder.setVector(severity.vector());
            list.add(builder.build());
        }
        return list;
    }

    private VexOuterClass.ScoreSource buildMethod(VulnerabilityRating severity) {
        if (severity.method() == null) {
            return null;
        }
        return switch (severity.method()) {
            case "3.1", "3.0", "CVSSv3" -> VexOuterClass.ScoreSource.CVSSv3;
            case "2.0", "CVSSv2" -> VexOuterClass.ScoreSource.CVSSv2;
            default -> VexOuterClass.ScoreSource.OTHER;
        };
    }

    private VexOuterClass.Severity buildSeverity(VulnerabilityRating severity) {
        if (severity.severity() == null) {
            return null;
        }
        return switch (severity.severity().toUpperCase()) {
            case "CRITICAL" -> VexOuterClass.Severity.CRITICAL;
            case "HIGH" -> VexOuterClass.Severity.HIGH;
            case "MEDIUM" -> VexOuterClass.Severity.MEDIUM;
            case "LOW" -> VexOuterClass.Severity.LOW;
            default -> VexOuterClass.Severity.NONE;
        };
    }

    private VexOuterClass.Score buildScore(VulnerabilityRating severity) {
        var builder = VexOuterClass.Score.newBuilder();
        builder.setBase(severity.baseScore());
        builder.setExploitability(severity.exploitabilityScore());
        builder.setImpact(severity.impactScore());
        return builder.build();
    }

    private VexOuterClass.Source buildSource(VulnerabilityReference vulnerabilityReference) {
        var builder = VexOuterClass.Source.newBuilder();
        builder.setUrl(vulnerabilityReference.source().value());
        builder.setName(vulnerabilityReference.type());
        return builder.build();
    }

    @Override
    public void rebuildDocument(Iterable<Vulnerability> vulnerabilities, String path) {
        logger.info("Writing VEX for " + path + "...");
        var builder = VexOuterClass.Vex.newBuilder();

        for (var vulnerability : vulnerabilities) {
            builder.addVulnerabilities(buildVulnerability(vulnerability));
        }

        try {
            var file = new File(path + ".vex.json");
            var outputStream = new FileWriter(file);
            outputStream.write(JsonFormat.printer().print(builder.build()));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON.", e);
        }
        logger.success("VEX updated.");
    }
}

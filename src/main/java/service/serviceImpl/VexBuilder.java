package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.vex.VexOuterClass;
import data.Component;
import data.Dependency;
import data.Vulnerability;
import data.VulnerabilityReference;
import data.VulnerabilitySeverity;
import logger.Logger;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VexBuilder implements DocumentBuilder {
    private static final Logger logger = Logger.of("VexBuilder");

    @Override
    public void buildDocument(Component root, String outputFileName) {
        var start = System.currentTimeMillis();

        logger.info("Creating SBOM for " + root.getQualifiedName() + "...");


        var vex = buildVex(root);

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
            var file = new File(outputFileName + ".vex.json");
            var outputStream = new FileWriter(file);
            outputStream.write(JsonFormat.printer().print(vex));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON.");
        }

        logger.success(new File(outputFileName).getAbsolutePath() + ".sbom.json saved (" + (System.currentTimeMillis() - start) + "ms)");
    }

    private VexOuterClass.Vex buildVex(Component root) {
        var builder = VexOuterClass.Vex.newBuilder();
        builder.addAllVulnerabilities(buildAllVulnerabilities(root));
        return builder.build();
    }

    private Iterable<VexOuterClass.Vulnerability> buildAllVulnerabilities(Component root) {
        var list = new ArrayList<VexOuterClass.Vulnerability>();

        var components = new ArrayList<>(root.getDependenciesFlat().stream().filter(Dependency::shouldResolveByScope).filter(Dependency::isNotOptional).map(Dependency::getComponent).filter(Objects::nonNull).toList());
        components.add(root);

        for (var component : components) {
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
        vulnerability.getAllReferences().stream().filter(ref -> ref.getType().equals("WEB")).findFirst().ifPresent(ref -> builder.setSource(buildSource(ref)));
        Optional.ofNullable(vulnerability.getAllSeverities()).ifPresent(severities -> builder.addAllRatings(buildAllRatings(severities)));
        Optional.ofNullable(vulnerability.getCwes()).ifPresent(cwes -> builder.addAllCwes(buildAllCwes(cwes)));
        builder.setDescription(vulnerability.getDetails());
        Optional.ofNullable(vulnerability.getRecommendations()).ifPresent(builder::addAllRecommendations);
        Optional.ofNullable(vulnerability.getAllReferences().stream().filter(ref -> ref.getType().equals("ADVISORY"))).ifPresent(refs -> builder.addAllAdvisories(refs.map(VulnerabilityReference::getUrl).toList()));
        return builder.build();
    }

    private Iterable<VexOuterClass.Cwe> buildAllCwes(List<String> cwes) {
        var list = new ArrayList<VexOuterClass.Cwe>();
        for (var cwe : cwes) {
            var builder = VexOuterClass.Cwe.newBuilder();
            builder.setCwe(Integer.parseInt(cwe.substring(cwe.lastIndexOf("-") + 1)));
            list.add(builder.build());
        }
        return list;
    }

    private Iterable<VexOuterClass.Rating> buildAllRatings(List<VulnerabilitySeverity> vulnerabilitySeverities) {
        var list = new ArrayList<VexOuterClass.Rating>();
        for (var severity : vulnerabilitySeverities) {
            var builder = VexOuterClass.Rating.newBuilder();
            builder.setScore(buildScore(severity));
            builder.setSeverity(buildSeverity(severity));
            builder.setMethod(buildMethod(severity));
            builder.setVector(severity.getVector());
            list.add(builder.build());
        }
        return list;
    }

    private VexOuterClass.ScoreSource buildMethod(VulnerabilitySeverity severity) {
        return switch (severity.getMethod()) {
            case "3.1" -> VexOuterClass.ScoreSource.CVSSv3;
            case "3.0" -> VexOuterClass.ScoreSource.CVSSv3;
            case "2.0" -> VexOuterClass.ScoreSource.CVSSv2;
            default -> VexOuterClass.ScoreSource.UNRECOGNIZED;
        };
    }

    private VexOuterClass.Severity buildSeverity(VulnerabilitySeverity severity) {
        return switch (severity.getSeverity().toUpperCase()) {
            case "CRITICAL" -> VexOuterClass.Severity.CRITICAL;
            case "HIGH" -> VexOuterClass.Severity.HIGH;
            case "MEDIUM" -> VexOuterClass.Severity.MEDIUM;
            case "LOW" -> VexOuterClass.Severity.LOW;
            default -> VexOuterClass.Severity.NONE;
        };
    }

    private VexOuterClass.Score buildScore(VulnerabilitySeverity severity) {
        var builder = VexOuterClass.Score.newBuilder();
        builder.setBase(severity.getBase());
        builder.setExploitability(severity.getExploitability());
        builder.setImpact(severity.getImpact());
        return builder.build();
    }

    private VexOuterClass.Source buildSource(VulnerabilityReference vulnerabilityReference) {
        var builder = VexOuterClass.Source.newBuilder();
        builder.setUrl(vulnerabilityReference.getUrl());
        builder.setName(vulnerabilityReference.getType());
        return builder.build();
    }
}

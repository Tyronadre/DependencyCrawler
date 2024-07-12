import cyclonedx.sbom.Bom16;
import data.Component;
import data.readData.ReadVexComponent;
import logger.LogLevel;
import logger.Logger;
import org.spdx.library.model.SpdxDocument;
import repository.LicenseRepository;
import repository.repositoryImpl.LicenseCollisionRepositoryImpl;
import service.DocumentBuilder;
import service.serviceImpl.BFDependencyCrawlerImpl;
import service.serviceImpl.DefaultInputReader;
import service.serviceImpl.LicenseCollisionBuilder;
import service.serviceImpl.SBOMBuilder;
import service.serviceImpl.SBOMReader;
import service.serviceImpl.SPDXBuilder;
import service.serviceImpl.SPDXReader;
import service.serviceImpl.TreeBuilder;
import service.serviceImpl.VexBuilder;
import service.serviceImpl.VexReader;
import settings.Settings;
import util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Main {
    private static final Logger logger = Logger.of("Main");

    public static void main(String[] args) {
//        args = new String[]{"--input", "src/main/resources/input_0.json"};
//        args = new String[]{"--input", "generated/output_0.sbom.json", "--input-type", "sbom", "--output", "generated/output_0_renewFromSBOM", "--output-type", "sbom", "spdx", "vex", "tree", "license-collisions", "--verbose"};
//        args = new String[]{"--input", "generated/output_0.spdx.json", "--input-type", "spdx", "--output", "generated/output_0_renewFromSPDX", "--output-type", "sbom", "spdx", "vex", "tree", "license-collisions", "--verbose"};
//        args = new String[]{"--input", "src/main/resources/input_2.json", "--output", "generated/output_2", "--output-type", "sbom", "spdx", "vex", "tree", "license-collisions", "--verbose"};
//        args = new String[]{"--input", "src/main/resources/input_1.json", "--output", "generated/output_1", "--crawl-all"};
//        args = new String[]{"--input", "src/main/resources/photoprism.json", "--output", "testoutput/output_0"};

        HashMap < String, String> argMap = new HashMap<>();
        String lastKey = null;
        for (String arg : args) {
            if (arg.startsWith("--")) {
                argMap.put(arg.substring(2), "");
                lastKey = arg.substring(2);
            } else {
                if (lastKey == null) {
                    logger.error("Invalid argument: " + arg);
                    return;
                }
                if (argMap.get(lastKey).isEmpty())
                    argMap.put(lastKey, arg);
                else
                    argMap.put(lastKey, argMap.get(lastKey) + ";" + arg);
            }
        }
        var legalArgs = List.of("input", "output", "input-type", "output-type", "help", "log-level", "no-log", "crawl-optional","crawl-all","crawl-threads", "data-folder");
        var illegalArgs = argMap.keySet().stream().filter(s -> !legalArgs.contains(s)).toList();
        if (!illegalArgs.isEmpty()) {
            logger.error("Illegal Arguments: " + illegalArgs);
            return;
        }

        if (argMap.isEmpty() || argMap.containsKey("help")) {
            printHelp();
            return;
        }

        if (argMap.containsKey("no-log")) {
            Logger.setLevel(null);
        }

        if (argMap.containsKey("log-level")) {
            try {
                Logger.setLevel(LogLevel.valueOf((argMap.get("log-level").toUpperCase())));
            } catch (IllegalArgumentException e) {
                logger.error("Illegal log level: " + argMap.get("log-level") + ". Allowed values are: " + Arrays.toString(LogLevel.values()));
            }
        }

        if (argMap.containsKey("crawl-optional")) {
            logger.info("Will crawl optional dependencies");
            Settings.crawlOptional = true;
        }

        if (argMap.containsKey("crawl-all")) {
            logger.info("Will crawl all dependencies");
            Settings.crawlEverything = true;
        }

        if (argMap.containsKey("crawl-threads")) {
            Settings.crawlThreads = Integer.parseInt(argMap.get("crawl-threads"));
        }

        if (argMap.containsKey("data-folder")) {
            if (!Settings.setDataFolder(new File(argMap.get("data-folder")))) {
                logger.error("Could not create data folder.");
                return;
            }
        }

        if (!argMap.containsKey("input")) {
            logger.error("No input file specified. Use --input <input file> to specify an input file.");
            return;
        }

        var inputFile = argMap.get("input");
        String inputType = null;
        if (argMap.containsKey("input-type")) {
            inputType = argMap.get("input-type");
        }

        var outputFile = argMap.getOrDefault("output", "output");
        var outputTypes = new ArrayList<String>();
        if (argMap.containsKey("output-type")) {
            outputTypes.addAll(Arrays.asList(argMap.get("output-type").split(";")));
        }

        if (outputTypes.isEmpty()) {
            if (inputType == null || inputType.equals("default")) {
                outputTypes.add("sbom");
                outputTypes.add("spdx");
                outputTypes.add("vex");
                outputTypes.add("license-collisions");
            } else {
                outputTypes.add(inputType);
            }
        }

        if (!Objects.equals(inputType, "vex")) {
            logger.info("Loading license repository...");
            LicenseRepository.getInstance(); //preload license repository
        }

        if (inputType == null) inputType = "default";
        switch (inputType) {
            case "default" -> readFromDefault(inputFile, outputFile, outputTypes);
            case "sbom" -> readFromSBOM(inputFile, outputFile, outputTypes);
            case "spdx" -> readFromSPDX(inputFile, outputFile, outputTypes);
            case "vex" -> {
                if (outputTypes.stream().anyMatch(it -> !it.equals("vex"))) {
                    logger.error("Can only generate VEX Files from a read VEX file");
                    throw new IllegalArgumentException("Can only generate VEX Files from a read VEX file");
                }
                readFromVex(inputFile, outputFile);
            }
            default -> {
                logger.error(inputType + " is not a valid inputFormat");
                throw new IllegalArgumentException(inputType + " is not a valid inputFormat");
            }
        }
    }

    private static void readFromDefault(String inputFile, String outputFile, ArrayList<String> outputTypes) {
        Component rootComponent;
        try {
            rootComponent = new DefaultInputReader().readDocument(inputFile);
        } catch (Exception e) {
            logger.error("Error reading input file: ", e);
            return;
        }
        if (rootComponent == null) return;

        new BFDependencyCrawlerImpl().crawl(rootComponent, true);


        for (var outputType : outputTypes) {
            try {
                if (Objects.equals(outputType, "license-collisions"))
                    new LicenseCollisionBuilder().buildDocument(LicenseCollisionRepositoryImpl.getInstace().checkLicenseCollisions(rootComponent), outputFile);
                else
                    getDocumentBuilder(outputType).buildDocument(rootComponent, outputFile);
            } catch (Exception e) {
                logger.error("Error building file type: " + outputType, e);
            }
        }
    }

    private static void readFromSBOM(String inputFile, String outputFile, ArrayList<String> outputTypes) {
        Pair<Bom16.Bom, Component> data;
        try {
            data = new SBOMReader().readDocument(inputFile);
        } catch (Exception e) {
            logger.error("Error reading SBOM file: ", e);
            return;
        }
        if (data == null) return;

        new BFDependencyCrawlerImpl().crawl(data.second(), false);

        for (var outputType : outputTypes) {
            try {
                if (Objects.equals(outputType, "sbom")) {
                    new SBOMBuilder().rebuildDocument(data.first(), outputFile);
                } else if (Objects.equals(outputType, "license-collisions")) {
                    new LicenseCollisionBuilder().buildDocument(LicenseCollisionRepositoryImpl.getInstace().checkLicenseCollisions(data.second()), outputFile);
                } else {
                    getDocumentBuilder(outputType).buildDocument(data.second(), outputFile);
                }
            } catch (Exception e) {
                logger.error("Error reading SBOM file: ", e);
            }
        }
    }

    private static void readFromSPDX(String inputFile, String outputFile, ArrayList<String> outputTypes) {
        Pair<SpdxDocument, Component> data;
        try {
            data = new SPDXReader().readDocument(inputFile);
        } catch (Exception e) {
            logger.error("Error reading SPDX file: ", e);
            return;
        }
        if (data == null) return;

        new BFDependencyCrawlerImpl().crawl(data.second(), false);

        for (var outputType : outputTypes) {
            try {
                if (Objects.equals(outputType, "spdx")) {
                    new SPDXBuilder().rebuildDocument(data, outputFile);
                } else if (Objects.equals(outputType, "license-collisions")) {
                    new LicenseCollisionBuilder().buildDocument(LicenseCollisionRepositoryImpl.getInstace().checkLicenseCollisions(data.second()), outputFile);
                } else {
                    getDocumentBuilder(outputType).buildDocument(data.second(), outputFile);
                }
            } catch (Exception e) {
                logger.error("Error reading SPDX file: ", e);
            }
        }
    }

    private static void readFromVex(String inputFile, String outputFile) {
        try {
            var res = new VexReader().readDocument(inputFile);
            res.forEach(ReadVexComponent::loadComponent);
            new VexBuilder().rebuildDocument(res.stream().map(Component::getAllVulnerabilities).flatMap(Collection::stream).toList(), outputFile);
        } catch (Exception e) {
            logger.error("Error reading VEX file: ", e);
        }
    }


    private static DocumentBuilder<Component, ?> getDocumentBuilder(String outputType) {
        return switch (outputType) {
            case "sbom" -> new SBOMBuilder();
            case "spdx" -> new SPDXBuilder();
            case "vex" -> new VexBuilder();
            case "tree-all" -> new TreeBuilder(true);
            case "tree" -> new TreeBuilder(false);
            default -> {
                logger.error(outputType + " is not a valid outputFormat");
                throw new IllegalArgumentException(outputType + " is not a valid outputFormat");
            }
        };
    }

    private static void printHelp() {
        logger.normal("""

                Reads a specified input file and outputs it in the specified format(s).

                Per default, reads a JSON file in the custom input format and outputs a SBOM, SPDX and VEX file.
                The default input file format is specified in the ReadMe.md of the GitRepository.

                If an input-type is specified, the input file is read in the specified format and then updated.
                If the input-type is vex, the only possible output type is vex.

                Usage:
                --input <file> :                                    input file in JSON format
                --output <file name> :                  [output]    output file name
                --input-type <type> :                   [default]   type of the input file. Supported types: default, sbom, spdx, vex.
                --output-type <type1> [<type2> ...] :   [sbom, spdx, vex, license-collisions]
                                                                    one or multiple output types. Supported types: sbom, spdx, vex, tree, tree-all, license-collisions
                --no-log :                              [false]     disable logging.
                --log-level :                           [INFO]      what level of logs should be shown. Supported types: ERROR, SUCCESS, INFO.
                --crawl-optional :                      [false]     crawl dependencies flagged as optional (maven).
                --crawl-all :                           [false]     crawl all dependencies, regardless of scope and optional (maven).
                --crawl-threads :                       [20]        number of threads for crawling.
                --data-folder :                         [crntDir]   changed the location of the data folder.

                --help :                                print this help message
                """);
    }

}

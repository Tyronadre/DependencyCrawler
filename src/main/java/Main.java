import cyclonedx.sbom.Bom16;
import data.Component;
import data.readData.ReadVexComponent;
import logger.Logger;
import repository.LicenseRepository;
import service.BFDependencyCrawler;
import service.DocumentBuilder;
import service.LicenseCollisionService;
import service.serviceImpl.BFDependencyCrawlerImpl;
import service.serviceImpl.DefaultInputReader;
import service.serviceImpl.MavenSBOMBuilder;
import service.serviceImpl.MavenSBOMReader;
import service.serviceImpl.SPDXBuilder;
import service.serviceImpl.SPDXReader;
import service.serviceImpl.TreeBuilder;
import service.serviceImpl.VexBuilder;
import service.serviceImpl.VexReader;
import util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Main {
    private static final Logger logger = Logger.of("Main");

    public static void main(String[] args) {
//        args = new String[]{"--input", "generated/output_1.spdx.json", "--input-type", "spdx", "--output", "generated/output_1_renewFromSPDX", "--output-type", "sbom", "spdx", "vex", "tree", "--verbose"};
        args = new String[]{"--input", "src/main/resources/input_2.json", "--output", "generated/output_2", "--output-type", "sbom", "spdx", "vex", "tree", "--verbose"};

        HashMap<String, String> argMap = new HashMap<>();
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
        var illegalArgs = argMap.keySet().stream().filter(s -> !(s.equals("input") || s.equals("output") || s.equals("input-type") || s.equals("output-type") || s.equals("help") || s.equals("verbose") || s.equals("no-log"))).toList();
        if (!illegalArgs.isEmpty()) {
            logger.error("Illegal Arguments: " + illegalArgs);
            return;
        }

        if (argMap.containsKey("help") || argMap.isEmpty()) {
            printHelp();
            return;
        }

        if (argMap.containsKey("no-log")) {
            Logger.setDisabled(true);
        }

        if (argMap.containsKey("verbose")) {
            Logger.setVerbose(true);
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
            } else {
                outputTypes.add(inputType);
            }
        }

        if (!Objects.equals(inputType, "vex")) {
            logger.info("Loading license repository...");
            LicenseRepository.getInstance(); //preload license repository
        }

        switch (inputType) {
            case "default" -> readFromDefault(inputFile, outputFile, outputTypes);
            case null -> readFromDefault(inputFile, outputFile, outputTypes);
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



//
//        var in2 = readInputFile("input_1.json");
//        crawlComponent(in2);
//        buildSBOMFile(in2, "generated/output_1");
//        buildSPDXFile(in2, "generated/output_1");
//        buildTreeFile(in2, "generated/output_1", false);
//        buildVexFile(in2, "generated/output_1");
//
//        var in3 = readInputFile("input_2.json");
//        crawlComponent(in3);
//        buildSBOMFile(in3, "generated/output_2");
//        buildSPDXFile(in3, "generated/output_2");
//        buildTreeFile(in3, "generated/output_2", false);
//        buildVexFile(in3, "generated/output_2");


//        var rein1 = readSBOMFile("generated/output_0.sbom.json");
//        crawlComponent(rein1.second());
//        writeSBOMFile(rein1.first(), "generated/output_0_rebuild");
//        buildSPDXFile(rein1.second(), "generated/output_0_rebuild");
//        buildTreeFile(rein1.second(), "generated/output_0_rebuild", false);
//        buildVexFile(rein1.second(), "generated/output_0_rebuild");

//        var rein2 = readSBOMFile("generated/output_1.sbom.json");
//        crawlComponent(rein2.second());
//        writeSBOMFile(rein2.first(), "generated/output_1_rebuild");
//        buildSPDXFile(rein2.second(), "generated/output_1_rebuild");
//        buildTreeFile(rein2.second(), "generated/output_1_rebuild", false);
//        buildVexFile(rein2.second(), "generated/output_1_rebuild");

//        var rein3 = readSBOMFile("generated/output_2.sbom.json");
//        crawlComponent(rein3.second());
//        writeSBOMFile(rein3.first(), "generated/output_2_rebuild");
//        buildSPDXFile(rein3.second(), "generated/output_2_rebuild");
//        buildTreeFile(rein3.second(), "generated/output_2_rebuild", false);
//        buildVexFile(rein3.second(), "generated/output_2_rebuild");
//
//        var vexComps = readVEXFile("generated/output_1.vex.json");
//        vexComps.forEach(ReadVexComponent::loadComponent);
//        writeVexFile(vexComps, "generated/output_1_rebuild");

    }

    private static void readFromDefault(String inputFile, String outputFile, ArrayList<String> outputTypes) {
        try {
            var rootComponent = new DefaultInputReader().readDocument(inputFile);
            crawlComponent(rootComponent);
            outputTypes.stream().map(Main::getDocumentBuilder).forEach(
                    builder -> builder.buildDocument(rootComponent, outputFile)
            );
        } catch (Exception e) {
            logger.error("Error reading input file: ", e);
        }
    }

    private static void readFromSBOM(String inputFile, String outputFile, ArrayList<String> outputTypes) {
        try {
            var res = new MavenSBOMReader().readDocument(inputFile);
            var rootComponent = res.second();
            crawlComponent(rootComponent);
            for (var outputType : outputTypes) {
                if (Objects.equals(outputType, "sbom")) {
                    new MavenSBOMBuilder().rebuildDocument(res.first(), outputFile);
                } else {
                    getDocumentBuilder(outputType).buildDocument(rootComponent, outputFile);
                }
            }
        } catch (Exception e) {
            logger.error("Error reading SBOM file: ", e);
        }
    }

    private static void readFromSPDX(String inputFile, String outputFile, ArrayList<String> outputTypes) {
        try {
            var rootComponent = new SPDXReader().readDocument(inputFile);
            crawlComponent(rootComponent);
            for (var outputType : outputTypes) {
                if (Objects.equals(outputType, "spdx")) {
                    new SPDXBuilder().rebuildDocument(rootComponent, outputFile);
                } else {
                    getDocumentBuilder(outputType).buildDocument(rootComponent, outputFile);
                }
            }
        } catch (Exception e) {
            logger.error("Error reading SPDX file: ", e);
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


    private static DocumentBuilder<?> getDocumentBuilder(String outputType) {
        return switch (outputType) {
            case "sbom" -> new MavenSBOMBuilder();
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

    private static void crawlComponent(Component component) {
        BFDependencyCrawler bfDependencyCrawler = new BFDependencyCrawlerImpl();
        LicenseCollisionService licenseCollisionService = LicenseCollisionService.getInstance();
        bfDependencyCrawler.crawl(component);
        licenseCollisionService.checkLicenseCollisions(component);
    }

    private static void writeSBOMFile(Bom16.Bom bom, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.rebuildDocument(bom, path);
    }

    private static void writeVexFile(List<ReadVexComponent> components, String path) {
        VexBuilder vexBuilder = new VexBuilder();
        vexBuilder.rebuildDocument(components.stream().map(Component::getAllVulnerabilities).flatMap(Collection::stream).toList(), path);
    }

    private static void buildSBOMFile(Component component, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.buildDocument(component, path);
    }

    private static void buildSPDXFile(Component component, String path) {
        SPDXBuilder spdxBuilder = new SPDXBuilder();
        spdxBuilder.buildDocument(component, path);
    }

    private static void buildTreeFile(Component component, String path, boolean showUnresolved) {
        TreeBuilder treeBuilder = new TreeBuilder(showUnresolved);
        treeBuilder.buildDocument(component, path);
    }

    private static void buildVexFile(Component component, String path) {
        VexBuilder vexBuilder = new VexBuilder();
        vexBuilder.buildDocument(component, path);
    }

    private static Component readInputFile(String fileName) {
        DefaultInputReader inputReader = new DefaultInputReader();
        return inputReader.readDocument(fileName);
    }


    private static Pair<Bom16.Bom, Component> readSBOMFile(String fileName) {
        MavenSBOMReader sbomReader = new MavenSBOMReader();
        return sbomReader.readDocument(fileName);
    }

    private static List<ReadVexComponent> readVEXFile(String fileName) {
        VexReader vexReader = new VexReader();
        return vexReader.readDocument(fileName);
    }

    private static Component readSPDXFile(String fileName) {
        SPDXReader spdxReader = new SPDXReader();
        return spdxReader.readDocument(fileName);
    }

    private static void printHelp() {
        logger.normal("""
                                
                Reads a specified input file and outputs it in the specified format(s).
                                
                Per default, reads a JSON file in the custom input format and outputs a SBOM file.
                The default input file format is specified in the ReadMe.md of the GitRepository.

                If an input-type is specified, the input file is read in the specified format and then updated.
                If the input-type is vex, this program will be able to output a VEX file.
                                
                Usage:
                --input <file> :                        input file in JSON format
                --output <file name> :                  output file name
                --input-type <type> :                   type of the input file. Supported types: sbom, spdx, vex
                --output-type <type1> [<type2> ...] :   one or multiple output types. Supported types: sbom, spdx, tree, vex
                --help :                                print this help message
                --verbose :                             print verbose output
                --no-log :                              disable logging
                """);
    }

}

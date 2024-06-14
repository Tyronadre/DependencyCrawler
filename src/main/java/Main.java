import cyclonedx.sbom.Bom16;
import data.Component;
import data.readData.ReadVexComponent;
import repository.LicenseRepository;
import service.BFDependencyCrawler;
import service.InputReader;
import service.LicenseCollisionService;
import service.serviceImpl.BFDependencyCrawlerImpl;
import service.serviceImpl.MavenSBOMBuilder;
import service.serviceImpl.MavenSBOMReader;
import service.serviceImpl.SPDXBuilder;
import service.serviceImpl.TreeBuilder;
import service.serviceImpl.VexBuilder;
import service.serviceImpl.VexReader;
import util.Pair;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

public class Main {
    public static void main(String[] args) throws URISyntaxException {

//        Logger.setDisabled(true);
//        Logger.setVerbose(false);
        LicenseRepository.getInstance(); //preload license repository


//        var in1 = readInputFile("input_0.json");
//        crawlComponent(in1);
//        buildSBOMFile(in1, "generated/output_0");
//        buildSPDXFile(in1, "generated/output_0");
//        buildTreeFile(in1, "generated/output_0", false);
//        buildVexFile(in1, "generated/output_0");
//
        var in2 = readInputFile("input_1.json");
        crawlComponent(in2);
        buildSBOMFile(in2, "generated/output_1");
        buildSPDXFile(in2, "generated/output_1");
        buildTreeFile(in2, "generated/output_1", false);
        buildVexFile(in2, "generated/output_1");
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

    private static Component readInputFile(String fileName) throws URISyntaxException {
        InputReader inputReader = InputReader.of(new File(Main.class.getClassLoader().getResource(fileName).toURI()));
        return inputReader.loadRootComponent();
    }


    private static Pair<Bom16.Bom, Component> readSBOMFile(String fileName) {
        MavenSBOMReader sbomReader = new MavenSBOMReader();
        return sbomReader.readDocument(fileName);
    }

    private static List<ReadVexComponent> readVEXFile(String fileName) {
        VexReader vexReader = new VexReader();
        return vexReader.readDocument(fileName);
    }

}

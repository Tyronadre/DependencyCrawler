import cyclonedx.sbom.Bom16;
import data.Component;
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
import util.Pair;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {

//        Logger.setDisabled(true);
//        Logger.setVerbose(false);
        LicenseRepository.getInstance(); //preload license repository


//        var in1 = readInputFile("input_0.json");
//        crawlComponent(in1);
//        buildSBOMFile(in1, "generated/output_0");
//        buildSPDXFile(in1, "generated/output_0");
//        buildTreeFile(in1, "generated/output_0");
//        buildVexFile(in1, "generated/output_0");
//
//        var in2 = readInputFile("input_1.json");
//        crawlComponent(in2);
//        buildSBOMFile(in2, "generated/output_1");
//        buildSPDXFile(in2, "generated/output_1");
//        buildTreeFile(in2, "generated/output_1");
//        buildVexFile(in2, "generated/output_1");
//
//        var in3 = readInputFile("input_2.json");
//        crawlComponent(in3);
//        buildSBOMFile(in3, "generated/output_2");
//        buildSPDXFile(in3, "generated/output_2");
//        buildTreeFile(in3, "generated/output_2");
//        buildVexFile(in3, "generated/output_2");


        var rein1 = readSBOMFile("generated/output_0.sbom.json");
        crawlComponent(rein1.second());
        writeSBOMFile(rein1.first(), "generated/output_0_rebuild");

//        buildSPDXFile(updatedRein1, "generated/output_0_rebuild");
//        buildTreeFile(updatedRein1, "generated/output_0_rebuild");
//        buildVexFile(updatedRein1, "generated/output_0_rebuild");

    }

    private static void crawlComponent(Component component) {
        BFDependencyCrawler bfDependencyCrawler = new BFDependencyCrawlerImpl();
        LicenseCollisionService licenseCollisionService = LicenseCollisionService.getInstance();
        bfDependencyCrawler.crawl(component, true);
        licenseCollisionService.checkLicenseCollisions(component);
    }

    private static void writeSBOMFile(Bom16.Bom bom, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.rebuildDocument(bom, path);
    }

    private static void buildSBOMFile(Component component, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.buildDocument(component, path);
    }

    private static void buildSPDXFile(Component component, String path) {
        SPDXBuilder spdxBuilder = new SPDXBuilder();
        spdxBuilder.buildDocument(component, path);
    }

    private static void buildTreeFile(Component component, String path) {
        TreeBuilder treeBuilder = new TreeBuilder();
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

}

import cyclonedx.sbom.Bom16;
import data.Component;
import data.dataImpl.MavenComponent;
import data.dataImpl.MavenDependency;
import repository.LicenseRepository;
import repository.repositoryImpl.MavenRepositoryType;
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
//        writeSBOMFile(in1, "generated/output_0");
//        writeSPDXFile(in1, "generated/output_0");
//        writeTreeFile(in1, "generated/output_0");
//        writeVexFile(in1, "generated/output_0");
//
//        var in2 = readInputFile("input_1.json");
//        writeSBOMFile(in2, "generated/output_1");
//        writeSPDXFile(in2, "generated/output_1");
//        writeTreeFile(in2, "generated/output_1");
//        writeVexFile(in2, "generated/output_1");
//
//        var in3 = readInputFile("input_2.json");
//        writeSBOMFile(in3, "generated/output_2");
//        writeSPDXFile(in3, "generated/output_2");
//        writeTreeFile(in3, "generated/output_2");
//        writeVexFile(in3, "generated/output_2");


        var rein1 = readSBOMFile("generated/output_0.sbom.json");
        updateComponent(rein1.second());
        writeSBOMFile(rein1.first(), "generated/output_0_rebuild");
//        buildSPDXFile(updatedRein1, "generated/output_0_rebuild");
//        buildTreeFile(updatedRein1, "generated/output_0_rebuild");
//        buildVexFile(updatedRein1, "generated/output_0_rebuild");

    }

    private static Component updateComponent(Component rein1) {
        var root = new MavenComponent(rein1.getGroup(), rein1.getName(), rein1.getVersion(), MavenRepositoryType.of(MavenRepositoryType.ROOT));
        root.setRoot();
        for (var dep : rein1.getDependencies()) {
            var depSplit = dep.getQualifiedName().split(":");
            root.addDependency(new MavenDependency(depSplit[0], depSplit[1], dep.getVersion(), root));
        }

        BFDependencyCrawler bfDependencyCrawler = new BFDependencyCrawlerImpl();
        LicenseCollisionService licenseCollisionService = LicenseCollisionService.getInstance();
        bfDependencyCrawler.crawl(root, true);
        licenseCollisionService.checkLicenseCollisions(root);
        return root;
    }

    private static void writeSBOMFile(Bom16.Bom bom, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.writeDocument(bom, path);
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
        LicenseCollisionService licenseCollisionService = LicenseCollisionService.getInstance();
        BFDependencyCrawler bfDependencyCrawler = new BFDependencyCrawlerImpl();

        var rootComponent = inputReader.loadRootComponent();
        bfDependencyCrawler.crawl(rootComponent, true);
        licenseCollisionService.checkLicenseCollisions(rootComponent);

        return rootComponent;
    }


    private static Pair<Bom16.Bom, Component> readSBOMFile(String fileName) {
        MavenSBOMReader sbomReader = new MavenSBOMReader();
        return sbomReader.readDocument(fileName);
    }

}

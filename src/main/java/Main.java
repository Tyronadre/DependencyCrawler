import data.Component;
import data.dataImpl.MavenComponent;
import data.dataImpl.MavenDependency;
import repository.LicenseRepository;
import repository.repositoryImpl.MavenRepositoryType;
import service.BFDependencyCrawler;
import service.DocumentReader;
import service.InputReader;
import service.LicenseCollisionService;
import service.serviceImpl.BFDependencyCrawlerImpl;
import service.serviceImpl.MavenSBOMBuilder;
import service.serviceImpl.MavenSBOMReader;
import service.serviceImpl.SPDXBuilder;
import service.serviceImpl.TreeBuilder;
import service.serviceImpl.VexBuilder;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {

//        Logger.setDisabled(true);
//        Logger.setVerbose(false);
        LicenseRepository.getInstance(); //preload license repository


        var in1 = readInputFile("input_0.json");
        writeSBOMFile(in1, "generated/output_0");
        writeSPDXFile(in1, "generated/output_0");
        writeTreeFile(in1, "generated/output_0");
        writeVexFile(in1, "generated/output_0");

        var in2 = readInputFile("input_1.json");
        writeSBOMFile(in2, "generated/output_1");
        writeSPDXFile(in2, "generated/output_1");
        writeTreeFile(in2, "generated/output_1");
        writeVexFile(in2, "generated/output_1");

        var in3 = readInputFile("input_2.json");
        writeSBOMFile(in3, "generated/output_2");
        writeSPDXFile(in3, "generated/output_2");
        writeTreeFile(in3, "generated/output_2");
        writeVexFile(in3, "generated/output_2");


        var rein1 = readSBOMFile("generated/output_0.sbom.json");
        var updatedRein1 = updateComponent(rein1);
        writeSBOMFile(updatedRein1, "generated/output_0_rebuild");
        writeSPDXFile(updatedRein1, "generated/output_0_rebuild");
        writeTreeFile(updatedRein1, "generated/output_0_rebuild");
        writeVexFile(updatedRein1, "generated/output_0_rebuild");

        var rein2 = readSBOMFile("generated/output_1.sbom.json");
        var component2 = updateComponent(rein2);
        writeSBOMFile(component2, "generated/output_1_rebuild");
        writeSPDXFile(component2, "generated/output_1_rebuild");
        writeTreeFile(component2, "generated/output_1_rebuild");
        writeVexFile(component2, "generated/output_1_rebuild");

        var rein3 = readSBOMFile("generated/output_2.sbom.json");
        var component3 = updateComponent(rein3);
        writeSBOMFile(component3, "generated/output_2_rebuild");
        writeSPDXFile(component3, "generated/output_2_rebuild");
        writeTreeFile(component3, "generated/output_2_rebuild");
        writeVexFile(component3, "generated/output_2_rebuild");
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

    private static void writeSBOMFile(Component component, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.buildDocument(component, path);
    }

    private static void writeSPDXFile(Component component, String path) {
        SPDXBuilder spdxBuilder = new SPDXBuilder();
        spdxBuilder.buildDocument(component, path);
    }

    private static void writeTreeFile(Component component, String path) {
        TreeBuilder treeBuilder = new TreeBuilder();
        treeBuilder.buildDocument(component, path);
    }

    private static void writeVexFile(Component component, String path) {
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


    private static Component readSBOMFile(String fileName) {
        DocumentReader sbomReader = new MavenSBOMReader();
        return sbomReader.readDocument(fileName);
    }

}

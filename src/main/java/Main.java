import data.Component;
import repository.LicenseRepository;
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


//        fromFile("input_0.json");
//        fromFile("input_1.json");
//        fromFile("input_2.json");

        var component = readSBOMFile("generated/output_0.sbom.json");
        writeSBOMFile(component, "generated/output_0_0");
    }

    private static void writeSBOMFile(Component component, String path) {
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        sbomBuilder.buildDocument(component, path);
    }

    private static void fromFile(String fileName) throws URISyntaxException {
        InputReader inputReader = InputReader.of(new File(Main.class.getClassLoader().getResource(fileName).toURI()));
        MavenSBOMBuilder sbomBuilder = new MavenSBOMBuilder();
        SPDXBuilder spdxBuilder = new SPDXBuilder();
        TreeBuilder treeBuilder = new TreeBuilder();
        VexBuilder vexBuilder = new VexBuilder();
        LicenseCollisionService licenseCollisionService = LicenseCollisionService.getInstance();
        BFDependencyCrawler bfDependencyCrawler = new BFDependencyCrawlerImpl();

        var rootComponent = inputReader.loadRootComponent();
        bfDependencyCrawler.crawl(rootComponent, true);
        licenseCollisionService.checkLicenseCollisions(rootComponent);

        sbomBuilder.buildDocument(rootComponent, inputReader.getOutputFileName());
        spdxBuilder.buildDocument(rootComponent, inputReader.getOutputFileName());
        treeBuilder.buildDocument(rootComponent, inputReader.getOutputFileName());
        vexBuilder.buildDocument(rootComponent, inputReader.getOutputFileName());
    }

    private static Component readSBOMFile(String fileName) {
        DocumentReader sbomReader = new MavenSBOMReader();
        return sbomReader.readDocument(fileName);
    }

}

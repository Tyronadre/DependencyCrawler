import logger.DefaultLogger;
import logger.Logger;
import repository.LicenseRepository;
import repository.repositoryImpl.LicenseRepositoryImpl;
import service.BFDependencyCrawler;
import service.InputReader;
import service.LicenseCollisionService;
import service.serviceImpl.BFDependencyCrawlerImpl;
import service.serviceImpl.SBOMBuilder;
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


        fromFile("input_0.json");
        fromFile("input_1.json");
        fromFile("input_2.json");
    }

    private static void fromFile(String fileName) throws URISyntaxException {
        InputReader inputReader = InputReader.of(new File(Main.class.getClassLoader().getResource(fileName).toURI()));
        SBOMBuilder sbomBuilder = new SBOMBuilder();
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

}

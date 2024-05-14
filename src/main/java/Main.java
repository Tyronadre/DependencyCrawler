import service.BFDependencyCrawler;
import service.InputReader;
import service.serviceImpl.BFDependencyCrawlerImpl;
import service.serviceImpl.SBOMBuilder;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        InputReader inputReader = InputReader.of(new File(Main.class.getClassLoader().getResource("input_0.json").toURI()));
        SBOMBuilder sbomBuilder = new SBOMBuilder();
        BFDependencyCrawler bfDependencyCrawler = new BFDependencyCrawlerImpl();

        var rootComponent = inputReader.loadRootComponent();
        bfDependencyCrawler.crawl(rootComponent, false);

        sbomBuilder.createSBOM(rootComponent, inputReader.getOutputFileName()); //use filepath from input reader
    }

}

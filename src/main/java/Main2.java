import data.dataImpl.maven.MavenComponent;
import service.InputReader;
import service.serviceImpl.SBOMBuilder;
import service.serviceImpl.maven.MavenInputReader;

import java.io.File;
import java.net.URISyntaxException;

public class Main2 {
    public static void main(String[] args) throws URISyntaxException {
        InputReader inputReader = new MavenInputReader();
        SBOMBuilder sbomParser = new SBOMBuilder();
        //        sbomParser.createSBOM(inputReader.createRootComponentAndLoadDependencies(new File(Main2.class.getClassLoader().getResource("input_0.txt").toURI())), "output_0");
//        sbomParser.createSBOM(inputReader.createRootComponentAndLoadDependencies(new File(Main2.class.getClassLoader().getResource("input_1.txt").toURI())), "output_1");
//        sbomParser.createSBOM(inputReader.createRootComponentAndLoadDependencies(new File(Main2.class.getClassLoader().getResource("input_2.txt").toURI())), "output_2");
        sbomParser.createSBOM(inputReader.createRootComponentAndLoadDependencies(new File(Main2.class.getClassLoader().getResource("input_3.txt").toURI())), "output_3");
    }
}

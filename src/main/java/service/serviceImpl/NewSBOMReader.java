package service.serviceImpl;

import data.Component;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.BomParserFactory;
import service.DocumentReader;
import util.Pair;

import java.io.File;

public class NewSBOMReader implements DocumentReader<Pair<Bom, Component>> {
    @Override
    public Pair<Bom, Component> readDocument(String inputFileName) {

        var file = new File(inputFileName);

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        //parse from file
        var bom = parseBom(file);
        var root = convertBom(bom);

        return new Pair<>(bom, root);
    }

    private Bom parseBom(File file) {
        try {
            return BomParserFactory.createParser(file).parse(file);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse SBOM from file: " + file.getAbsolutePath(), e);
        }
    }

    private Component convertBom(Bom bom) {
        var components = bom.getComponents();

        for (org.cyclonedx.model.Dependency dependency : bom.getDependencies()) {
//            return new DependencyConverter().convert(component);
        }

        return null;

    }
}

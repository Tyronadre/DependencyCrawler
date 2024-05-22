package service.serviceImpl;

import data.Component;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileNotFoundException;

public class TreeBuilder implements DocumentBuilder {

    @Override
    public void buildDocument(Component root, String outputFileName) {
        logger.info("Writing tree for " + root.getQualifiedName() + "...");

        var outputFileDir = outputFileName.split("/", 2);
        if (outputFileDir.length > 1) {
            //create out dir if not exists
            File outDir = new File(outputFileDir[0]);
            if (!outDir.exists()) {
                outDir.mkdir();
            }
        }

        // tree
        try {
            root.printTree(outputFileName + ".tree.txt");
        } catch (FileNotFoundException e) {
            logger.errorLine("Failed writing to tree.");
        }

        logger.successLine("Done.");
    }
}

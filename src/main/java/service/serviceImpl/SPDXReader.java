package service.serviceImpl;

import data.Component;
import logger.Logger;
import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.storage.IModelStore;
import org.spdx.storage.ISerializableModelStore;
import org.spdx.storage.simple.InMemSpdxStore;
import service.DocumentReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SPDXReader implements DocumentReader<Component> {
    private static final Logger logger = Logger.of("SPDXReader");

    @Override
    public Component readDocument(String inputFileName) {
        logger.info("Reading document as SPDX: " + inputFileName);

        var file = new File(inputFileName);
        try (IModelStore modelStore = new InMemSpdxStore()) {

            // Create a Jackson store
            ISerializableModelStore jacksonStore = new MultiFormatStore(modelStore, MultiFormatStore.Format.JSON);

            // Parse the SPDX document content
            var documentURI = jacksonStore.deSerialize(new FileInputStream(file), true);

            // Use the SPDX document (for example, print the document name)

            System.out.println("SPDX Document Name: " + jacksonStore.getAllItems(documentURI, "SpdxDocument").findFirst().get());

        } catch (IOException e) {
            logger.error("Could not read from file: " + file.getAbsolutePath() + ". Cause: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("Could not parse from file: " + file.getAbsolutePath() + ". Cause: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }
}

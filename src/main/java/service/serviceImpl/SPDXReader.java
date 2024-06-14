package service.serviceImpl;

import data.Component;
import logger.Logger;
import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.library.InvalidSPDXAnalysisException;
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

        try (IModelStore modelStore = new InMemSpdxStore()) {
            var file = new File(inputFileName);

            // Create a Jackson store
            ISerializableModelStore jacksonStore = new MultiFormatStore(modelStore, MultiFormatStore.Format.JSON);

            // Parse the SPDX document content
            jacksonStore.deSerialize(new FileInputStream(file), true);

            // Use the SPDX document (for example, print the document name)
            System.out.println("SPDX Document Name: " + jacksonStore.getDocumentUris());

        } catch (IOException e) {
            System.err.println("Error reading the SPDX file: " + e.getMessage());
        } catch (
                InvalidSPDXAnalysisException e) {
            System.err.println("Error parsing the SPDX document: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

package service;

import cyclonedx.sbom.Bom16;
import data.Component;
import logger.Logger;

public interface DocumentBuilder <T> {
    Logger logger = Logger.of("DocumentBuilder");

    void buildDocument(Component root, String outputFileName);

    void writeDocument(T bom, String path);
}

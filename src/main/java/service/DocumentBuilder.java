package service;

import data.Component;
import logger.AppendingLogger;
import logger.Logger;

public interface DocumentBuilder {
    Logger logger = Logger.of("DocumentBuilder");

    void buildDocument(Component root, String outputFileName);
}

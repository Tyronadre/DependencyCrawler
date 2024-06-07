package service;

import data.Component;
import logger.Logger;

public interface DocumentBuilder <T> {
    Logger logger = Logger.of("DocumentBuilder");

    void buildDocument(Component root, String outputFileName);

    default void rebuildDocument(T bom, String path){
        throw new UnsupportedOperationException("Not implemented");
    }
}

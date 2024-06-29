package service;

import logger.Logger;

public interface DocumentBuilder <V, T> {
    Logger logger = Logger.of("DocumentBuilder");

    void buildDocument(V data, String outputFileName);

    default void rebuildDocument(T data, String outputFileName){
        throw new UnsupportedOperationException("Not implemented");
    }
}

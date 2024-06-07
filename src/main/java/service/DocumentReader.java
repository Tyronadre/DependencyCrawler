package service;

import cyclonedx.sbom.Bom16;
import data.Component;
import util.Pair;

public interface DocumentReader<T> {
    Pair<T, Component> readDocument(String inputFileName);
}

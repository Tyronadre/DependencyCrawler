package service;

public interface DocumentReader<T> {
    T readDocument(String inputFileName);
}

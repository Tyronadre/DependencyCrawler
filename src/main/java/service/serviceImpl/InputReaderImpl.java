package service.serviceImpl;

import dependency_crawler.input.DependencyCrawlerInput;
import service.InputReader;

public abstract class InputReaderImpl implements InputReader {
    protected DependencyCrawlerInput.Input dependencyCrawlerInput;

    public InputReaderImpl(DependencyCrawlerInput.Input input) {
        this.dependencyCrawlerInput = input;
    }

}



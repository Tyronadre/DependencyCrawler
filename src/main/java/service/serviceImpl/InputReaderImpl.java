package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import data.Component;
import data.dataImpl.maven.MavenComponent;
import dependency_crawler.input.DependencyCrawlerInput;
import service.InputReader;
import service.serviceImpl.maven.MavenInputReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class InputReaderImpl implements InputReader {
    protected DependencyCrawlerInput.Input dependencyCrawlerInput;

    public InputReaderImpl(DependencyCrawlerInput.Input input) {
        this.dependencyCrawlerInput = input;
    }

}



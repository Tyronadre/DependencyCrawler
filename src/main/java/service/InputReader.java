package service;

import com.google.protobuf.util.JsonFormat;
import data.Component;
import dependency_crawler.input.DependencyCrawlerInput;
import service.serviceImpl.maven.MavenInputReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Reads an input file and creates the artifacts and dependencies.
 * The file will be from JSON format into {@link dependency_crawler.input.DependencyCrawlerInput}.
 */
public interface InputReader {

    Component loadRootComponent();

    String getOutputFileName();

    static InputReader of(File file) {
        try {
            var builder = DependencyCrawlerInput.Input.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(file.toPath(), StandardCharsets.UTF_8), builder);
            var input = builder.build();
            return switch (input.getApplication().getType()) {
                case JAVA -> new MavenInputReader(input);
                case C -> throw new IllegalArgumentException("Unsupported application type: C.");
                case OTHER -> throw new IllegalArgumentException("Unsupported application type: Other.");
                case UNRECOGNIZED -> throw new IllegalArgumentException("Application type could not be parsed!");
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

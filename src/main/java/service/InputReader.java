package service;

import com.google.protobuf.util.JsonFormat;
import cyclonedx.sbom.Bom16;
import data.Component;
import dependencyCrawler.DependencyCrawlerInput;
import logger.Logger;
import service.serviceImpl.MavenInputReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Reads an input file and creates the artifacts and dependencies.
 * The file will be from JSON format into {@link dependencyCrawler.DependencyCrawlerInput}.
 */
public interface InputReader {

    Component loadRootComponent();


    String getOutputFileName();

    static InputReader of(File file) {
        Logger logger = Logger.of("InputReader");
        logger.info("Reading input from " + file.getAbsolutePath() + "...");
        try {
            var builder = DependencyCrawlerInput.Input.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(file.toPath(), StandardCharsets.UTF_8), builder);
            var input = builder.build();
            logger.success("Input read successfully from " + file.getAbsolutePath());
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

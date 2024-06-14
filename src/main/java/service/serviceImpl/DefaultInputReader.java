package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import data.Component;
import data.internalData.MavenComponent;
import data.internalData.MavenDependency;
import data.internalData.MavenVersion;
import dependencyCrawler.DependencyCrawlerInput;
import logger.Logger;
import repository.repositoryImpl.MavenComponentRepositoryType;
import service.DocumentReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DefaultInputReader implements DocumentReader<Component> {
    private static final Logger logger = Logger.of("MavenInputReader");


    @Override
    public Component readDocument(String input) {
        logger.info("Reading input file...");

        var file = new File(input);
        DependencyCrawlerInput.Input dependencyCrawlerInput;
        try {
            var builder = DependencyCrawlerInput.Input.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(Files.readString(file.toPath(), StandardCharsets.UTF_8), builder);
            dependencyCrawlerInput = builder.build();
            logger.success("Input read successfully from " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error reading input file  " + file.getAbsolutePath() + " cause " + e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }

        logger.info("Reading file...");
        //check if the input application is a java application
        var application = dependencyCrawlerInput.getApplication();
        if (application.getType() != DependencyCrawlerInput.Type.JAVA) {
            logger.error("The input application is not a java application.");
            throw new IllegalArgumentException("The input application is not a java application.");
        }

        //get the parent artifact
        MavenComponent parentArtifact = (MavenComponent) MavenComponentRepositoryType.of(MavenComponentRepositoryType.ROOT).getComponent(application.getGroupId(), application.getName(), new MavenVersion(application.getVersion()));
        parentArtifact.setRoot();

        //read the dependencies
        for (var dependency : application.getDependenciesList()) {
            var mavenDependency = new MavenDependency(dependency.getGroupId(), dependency.getName(), new MavenVersion(dependency.getVersion()), parentArtifact);
            parentArtifact.addDependency(mavenDependency);
        }

        return parentArtifact;
    }
}

package service.serviceImpl.maven;

import data.dataImpl.maven.MavenComponent;
import data.dataImpl.maven.MavenDependency;
import data.dataImpl.maven.MavenVersion;
import dependency_crawler.input.DependencyCrawlerInput;
import logger.Logger;
import repository.repositoryImpl.MavenRepositoryType;
import service.serviceImpl.InputReaderImpl;

import java.io.File;
import java.util.Optional;

public class MavenInputReader extends InputReaderImpl {
    private static final Logger logger = Logger.of("MavenInputReader");

    public MavenInputReader(DependencyCrawlerInput.Input input) {
        super(input);
    }

    @Override
    public MavenComponent loadRootComponent() {
        logger.info("Reading file...");
        //check if the input application is a java application
        var application = dependencyCrawlerInput.getApplication();
        if (application.getType() != DependencyCrawlerInput.Type.JAVA) {
            logger.error("The input application is not a java application.");
            throw new IllegalArgumentException("The input application is not a java application.");
        }

        //get the parent artifact
        MavenComponent parentArtifact = (MavenComponent) MavenRepositoryType.of(MavenRepositoryType.ROOT).getComponent(application.getGroupId(), application.getName(), new MavenVersion(application.getVersion()));
        parentArtifact.setRoot();

        //read the dependencies
        for (var dependency : application.getDependenciesList()) {
            var mavenDependency = new MavenDependency(dependency.getGroupId(), dependency.getName(), new MavenVersion(dependency.getVersion()), parentArtifact);
            parentArtifact.addDependency(mavenDependency);
        }

        return parentArtifact;
    }

    @Override
    public String getOutputFileName() {
        return Optional.of(dependencyCrawlerInput.getOutput()).orElse("output");
    }
}

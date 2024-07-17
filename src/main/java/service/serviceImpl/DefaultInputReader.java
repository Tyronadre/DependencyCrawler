package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import data.Component;
import data.Version;
import data.internalData.AndroidNativeDependency;
import data.internalData.ConanDependency;
import data.internalData.JitPackDependency;
import data.internalData.MavenComponent;
import data.internalData.MavenDependency;
import dependencyCrawler.DependencyCrawlerInput;
import logger.Logger;
import repository.repositoryImpl.LicenseRepositoryImpl;
import repository.repositoryImpl.MavenComponentRepository;
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

        //save properties
        for (var parameter : dependencyCrawlerInput.getParametersList()) {
            if (parameter.getKey().startsWith("POM_FILE:"))
                MavenComponentRepository.getInstance().addCustomPomFile(parameter.getKey().substring(9), parameter.getValue());
        }

        var application = dependencyCrawlerInput.getApplication();

        //get the parent artifact
        MavenComponent parentArtifact = (MavenComponent) MavenComponentRepository.getInstance().getComponent(application.getGroupId(), application.getName(), Version.of(application.getVersion()), null);
        if (application.hasLicenseId()){
            parentArtifact.setData("licenseChoice", LicenseRepositoryImpl.getInstance().getLicenseChoice(application.getLicenseId(), null, application.getName()));
        }
        parentArtifact.setRoot();

        //read the dependencies
        for (var dependency : application.getDependenciesList()) {
            var newDependency = switch (dependency.getType()) {
                case OTHER -> {
                    logger.error("dependency type is specified as 'OTHER'. Skipping not supported " + dependency.getGroupId() + ":" + dependency.getName() + ":" + dependency.getVersion());
                    yield null;
                }
                case MAVEN ->
                        new MavenDependency(dependency.getGroupId(), dependency.getName(), Version.of(dependency.getVersion()), parentArtifact);
                case CONAN ->
                        new ConanDependency(dependency.getName(), Version.of(dependency.getVersion()), parentArtifact);
                case ANDROID_NATIVE ->
                        new AndroidNativeDependency(dependency.getGroupId(), dependency.getName(), Version.of(dependency.getVersion()), parentArtifact);
                case JITPACK ->
                        new JitPackDependency(dependency.getGroupId(), dependency.getName(), Version.of(dependency.getVersion()), parentArtifact);
                case UNRECOGNIZED -> {
                    logger.error("dependency type not recognized (" + dependency.getType() + "). Skipping not supported " + dependency.getGroupId() + ":" + dependency.getName() + ":" + dependency.getVersion());
                    yield null;
                }
            };
            if (newDependency != null)
                parentArtifact.addDependency(newDependency);
        }

        return parentArtifact;
    }
}

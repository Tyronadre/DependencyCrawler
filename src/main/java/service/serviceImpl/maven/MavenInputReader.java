package service.serviceImpl.maven;

import data.dataImpl.maven.MavenComponent;
import data.dataImpl.maven.MavenDependency;
import data.dataImpl.maven.MavenVersion;
import repository.repositoryImpl.MavenRepositoryType;
import service.InputReader;
import service.serviceImpl.BFDependencyCrawlerImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MavenInputReader implements InputReader {
    @Override
    public MavenComponent createRootComponentAndLoadDependencies(File file) {
        MavenComponent parentArtifact;
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            //skip first line
            fileReader.readLine();

            //read parent artifact
            String parentArtifactString = fileReader.readLine();
            String[] parentArtifactData = parentArtifactString.split(":");
            if (parentArtifactData.length != 3) {
                throw new IllegalArgumentException("Parent artifact data is not in the correct format.");
            }

            parentArtifact = (MavenComponent) MavenRepositoryType.of(MavenRepositoryType.ROOT).getComponent(parentArtifactData[0], parentArtifactData[1], new MavenVersion(parentArtifactData[2]));
            parentArtifact.setRoot();

            //read dependencies
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] dependencyData = line.split(":");
                if (dependencyData.length != 3) {
                    throw new IllegalArgumentException("Dependency data is not in the correct format: " + line);
                }
                var dependency = new MavenDependency(dependencyData[0], dependencyData[1], new MavenVersion(dependencyData[2]), parentArtifact);
                parentArtifact.addDependency(dependency);
            }

            //create the tree
            BFDependencyCrawlerImpl bfDependencyCrawler = new BFDependencyCrawlerImpl();
            bfDependencyCrawler.loadDependencies(parentArtifact);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return parentArtifact;
    }
}

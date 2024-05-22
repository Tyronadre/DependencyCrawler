package repository.repositoryImpl;

import data.Component;
import data.Dependency;
import data.Version;
import data.dataImpl.maven.MavenComponent;
import data.dataImpl.maven.MavenDependency;
import data.dataImpl.maven.MavenVersion;
import enums.RepositoryType;
import exceptions.ArtifactBuilderException;
import logger.Logger;
import repository.Repository;
import service.VersionRangeResolver;
import service.VersionResolver;
import service.serviceImpl.maven.MavenService;
import service.serviceImpl.maven.MavenVersionRangeResolver;
import service.serviceImpl.maven.MavenVersionResolver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * A Maven repository.
 * <p>
 * Example BaseURL: <a href="https://repo1.maven.org/maven2/">Maven Central</a>
 */
public class MavenRepository implements Repository {
    private final RepositoryType repositoryType;
    private final String baseUrl;
    private final MavenService mavenService;
    private final HashMap<String, Component> components;
    private final MavenVersionRangeResolver versionRangeResolver = new MavenVersionRangeResolver(this);
    private final MavenVersionResolver versionResolver = new MavenVersionResolver(this);

    private final Logger logger = Logger.of("Maven");

    MavenRepository(MavenRepositoryType repositoryType) {
        this.repositoryType = repositoryType;
        this.baseUrl = repositoryType.getUrl();
        mavenService = new MavenService();
        components = new HashMap<>();
    }


    @Override
    public String request(String request) {
        return "";
    }


    @Override
    public List<MavenVersion> getVersions(Dependency dependency) {
        var mavenDependency = (MavenDependency) dependency;
        try {
            return this.getVersions(URI.create(baseUrl + mavenDependency.getGroupId().replace(".", "/") + "/" + mavenDependency.getArtifactId() + "/maven-metadata.xml").toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<MavenVersion> getVersions(Component component) {
        var mavenComponent = (MavenComponent) component;
        try {
            return this.getVersions(URI.create(baseUrl + mavenComponent.getGroup().replace(".", "/") + "/" + mavenComponent.getName() + "/maven-metadata.xml").toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    private List<MavenVersion> getVersions(URL url) {
        return mavenService.getVersions(url).stream().map(MavenVersion::new).collect(Collectors.toList());
    }

    @Override
    public VersionResolver getVersionResolver() {
        return versionResolver;
    }

    @Override
    public boolean loadComponent(Component component) {
        if (this.repositoryType == MavenRepositoryType.ROOT) {
            return false;
        }
        var mavenComponent = (MavenComponent) component;
        try {
            mavenComponent.setModel(mavenService.loadModel(URI.create(getDownloadLocation(component) + ".pom").toURL()));
            logger.info(" +model ");
            mavenComponent.setHashes(mavenService.loadHashes(getDownloadLocation(component) + ".jar"));
            logger.info(" +hashes ");
            mavenComponent.setVulnerabilities(mavenService.loadVulnerabilities(mavenComponent));
            logger.info(" +vulnerabilities ");
            return true;
        } catch (MalformedURLException | ArtifactBuilderException e) {
            return false;
        }
    }

    @Override
    public Component getComponent(String groupId, String artifactId, Version version) {
        if (components.containsKey(groupId + ":" + artifactId + ":" + version.getVersion())) {
            return components.get(groupId + ":" + artifactId + ":" + version.getVersion());
        } else {
            var component = new MavenComponent(groupId, artifactId, version, this);
            components.put(groupId + ":" + artifactId + ":" + version.getVersion(), component);
            return component;
        }
    }

    @Override
    public VersionRangeResolver getVersionRangeResolver() {
        return versionRangeResolver;
    }

    @Override
    public RepositoryType getType() {
        return this.repositoryType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MavenRepository.class.getSimpleName() + "[", "]").add("'" + baseUrl + "'").toString();
    }

    public String getDownloadLocation(Component mavenComponent) {
        return baseUrl + mavenComponent.getGroup().replace(".", "/") + "/" + mavenComponent.getName() + "/" + mavenComponent.getVersion().getVersion() + "/" + mavenComponent.getName() + "-" + mavenComponent.getVersion().getVersion();
    }
}
package repository.repositoryImpl;

import data.Component;
import data.Dependency;
import data.Hash;
import data.Version;
import data.Vulnerability;
import data.internalData.MavenComponent;
import data.internalData.MavenDependency;
import data.internalData.MavenVersion;
import enums.RepositoryType;
import exceptions.ArtifactBuilderException;
import logger.Logger;
import org.apache.maven.api.model.Model;
import org.apache.maven.model.v4.MavenStaxReader;
import repository.ComponentRepository;
import repository.VulnerabilityRepository;
import service.VersionRangeResolver;
import service.VersionResolver;
import service.serviceImpl.MavenVersionRangeResolver;
import service.serviceImpl.MavenVersionResolver;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A Maven repository.
 * <p>
 * Example BaseURL: <a href="https://repo1.maven.org/maven2/">Maven Central</a>
 */
public class MavenComponentRepository implements ComponentRepository {
    private final RepositoryType repositoryType;
    private final String baseUrl;
    private final HashMap<String, TreeSet<Component>> components;
    private final MavenVersionRangeResolver versionRangeResolver = new MavenVersionRangeResolver(this);
    private final MavenVersionResolver versionResolver = new MavenVersionResolver(this);

    private static final Logger logger = Logger.of("MavenRepository");

    MavenComponentRepository(MavenComponentRepositoryType repositoryType) {
        this.repositoryType = repositoryType;
        this.baseUrl = repositoryType.getUrl();
        components = new HashMap<>();
    }

    @Override
    public List<MavenVersion> getVersions(Dependency dependency) {
        var mavenDependency = (MavenDependency) dependency;
        try {
            var urlString = baseUrl + mavenDependency.getGroupId().replace(".", "/") + "/" + mavenDependency.getArtifactId() + "/maven-metadata.xml";
            return this.getVersions(URI.create(urlString).toURL());
        } catch (MalformedURLException e) {
            logger.error("Failed to get versions. " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Returns the versions of the given URL as Strings
     *
     * @param url The URL to get the versions from
     * @return The versions as Strings
     */
    private List<MavenVersion> getVersions(URL url) {
        List<String> versions = null;

        var factory = XMLInputFactory.newInstance();
        try {
            XMLEventReader reader = factory.createXMLEventReader(url.openStream());
            while (reader.hasNext()) {
                var event = reader.nextEvent();
                if (event.isStartElement()) {
                    var startElement = event.asStartElement();
                    switch (startElement.getName().toString()) {
                        case "versioning":
                            versions = new ArrayList<>();
                            break;
                        case "version":
                            if (versions == null) break;
                            event = reader.nextEvent();
                            if (event.isCharacters()) {
                                versions.add(event.asCharacters().getData());
                            }
                            break;
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
        if (versions == null) {
            return List.of();
        }
        return versions.stream().map(MavenVersion::new).collect(Collectors.toList());
    }

    @Override
    public VersionResolver getVersionResolver() {
        return versionResolver;
    }

    @Override
    public boolean loadComponent(Component component) {
        if (this.repositoryType == MavenComponentRepositoryType.ROOT) {
            return false;
        }
        try {
            component.setData("model", loadModel(URI.create(getDownloadLocation(component) + ".pom").toURL()));
            component.setData("hashes", loadHashes(getDownloadLocation(component) + ".jar"));
            component.setData("vulnerabilities", loadVulnerabilities(component));
            return true;
        } catch (MalformedURLException | ArtifactBuilderException ignored) {

        }
        return false;
    }

    private Model loadModel(URL url) throws ArtifactBuilderException {
        MavenStaxReader reader = new MavenStaxReader();
        Model model;
        try (InputStream inputStream = url.openStream()) {
            model = reader.read(inputStream);
        } catch (IOException | XMLStreamException e) {
            throw new ArtifactBuilderException("Could not load model from " + url + ". " + e);
        }
        return model;
    }

    private List<Hash> loadHashes(String baseUrl) {
        var hashes = new ArrayList<Hash>();
        for (var algorithm : new String[]{"md5", "sha1", "sha256", "sha512"}) {
            try {
                hashes.add(loadHash(baseUrl, algorithm));
            } catch (IOException ignored) {
            }
        }
        return hashes;
    }

    private Hash loadHash(String baseUrl, String algorithm) throws IOException {
        try (InputStream inputStream = URI.create(baseUrl + "." + algorithm).toURL().openStream()) {

            var value = new String(inputStream.readAllBytes());
            // some files have some spaces and a - at the end. we dont want that
            if (value.contains(" "))
                value = value.substring(0, value.indexOf(" "));
            return Hash.of(algorithm, value);
        }
    }

    private List<Vulnerability> loadVulnerabilities(Component mavenComponent) {
        return VulnerabilityRepository.getInstance().getVulnerabilities(mavenComponent);
    }

    @Override
    public synchronized Component getComponent(String groupId, String artifactId, Version version) {
        if (!components.containsKey(groupId + ":" + artifactId)) {
            components.put(groupId + ":" + artifactId, new TreeSet<>(Comparator.comparing(Component::getVersion)));
        }
        var set = components.get(groupId + ":" + artifactId);
        var newComponent = new MavenComponent(groupId, artifactId, version, this);
        var component = set.floor(newComponent);
        if (component == null || !component.getVersion().equals(version)) {
            components.get(groupId + ":" + artifactId).add(newComponent);
            return newComponent;
        }
        return component;
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
        return new StringJoiner(", ", MavenComponentRepository.class.getSimpleName() + "[", "]").add("'" + baseUrl + "'").toString();
    }

    @Override
    public String getDownloadLocation(Component mavenComponent) {
        return baseUrl + mavenComponent.getGroup().replace(".", "/") + "/" + mavenComponent.getName() + "/" + mavenComponent.getVersion().getVersion() + "/" + mavenComponent.getName() + "-" + mavenComponent.getVersion().getVersion();
    }

    public TreeSet<Component> getLoadedComponents(String groupName, String artifactName) {
        return components.get(groupName + ":" + artifactName);
    }

}
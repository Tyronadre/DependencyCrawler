package repository.repositoryImpl;

import data.Component;
import data.Dependency;
import data.Hash;
import data.Version;
import data.internalData.MavenComponent;
import data.internalData.MavenDependency;
import enums.MavenComponentRepositoryType;
import logger.Logger;
import org.apache.maven.api.model.Model;
import org.apache.maven.model.v4.MavenStaxReader;
import repository.ComponentRepository;
import service.VersionResolver;
import service.serviceImpl.MavenVersionResolver;
import settings.Settings;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A Maven repository.
 * <p>
 * Example BaseURL: <a href="https://repo1.maven.org/maven2/">Maven Central</a>
 */
public class MavenComponentRepository implements ComponentRepository {
    private static final Logger logger = Logger.of("MavenRepository");
    private static final MavenComponentRepository instance = new MavenComponentRepository();

    private final HashMap<String, TreeSet<Component>> components = new HashMap<>();
    private final HashMap<Component, MavenComponentRepositoryType> types = new HashMap<>();
    private final HashMap<String, String> customPomFiles = new HashMap<>();

    private MavenComponentRepository() {
    }

    public static MavenComponentRepository getInstance() {
        return instance;
    }

    public void addCustomPomFile(String key, String filePath) {
        this.customPomFiles.put(key, filePath);
    }

    @Override
    public List<Version> getVersions(Dependency dependency) {
        logger.info("Getting all possible versions for " + dependency.getQualifiedName());

        var mavenDependency = (MavenDependency) dependency;

        try {
            var urlString = getRepositoryType(dependency.getTreeParent()).getUrl() + mavenDependency.getGroupId().replace(".", "/") + "/" + mavenDependency.getArtifactId() + "/maven-metadata.xml";
            return this.getVersions(URI.create(urlString).toURL());
        } catch (MalformedURLException e) {
            logger.error("Failed to get versions. " + e.getMessage());
        }
        return List.of();
    }

    /**
     * @param component the key
     * @return the type
     */
    public MavenComponentRepositoryType getRepositoryType(Component component) {
        return this.types.get(component);
    }

    /**
     * Returns the versions of the given URL as Strings
     *
     * @param url The URL to get the versions from
     * @return The versions as Strings
     */
    private List<Version> getVersions(URL url) {
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
        return versions.stream().map(Version::of).collect(Collectors.toList());
    }

    @Override
    public VersionResolver getVersionResolver() {
        return MavenVersionResolver.getInstance();
    }

    HashMap<String, Integer> loadStatus = new HashMap<>();

    @Override
    public int loadComponent(Component component) {
        var start = System.currentTimeMillis();
        logger.info("Loading component " + component.getQualifiedName());

        if (loadStatus.containsKey(component.getQualifiedName())) {
            logger.info("component was loaded already with status " + loadStatus.get(component.getQualifiedName()));
            return loadStatus.get(component.getQualifiedName());
        }

        //try to load from custom pom file
        if (customPomFiles.containsKey(component.getQualifiedName())) {
            try {
                logger.info("Loading for " + component.getQualifiedName() + " from custom POM file");
                component.setData("model", loadModelFromPom(customPomFiles.get(component.getQualifiedName())));
                types.put(component, MavenComponentRepositoryType.CUSTOM);
                loadStatus.put(component.getQualifiedName(), 0);
                return 0;
            } catch (IOException e) {
                logger.error("Could not read custom POM file of component: " + component.getQualifiedName(), e);
                loadStatus.put(component.getQualifiedName(), 1);
                return 1;
            } catch (XMLStreamException e) {
                logger.error("Could not parse custom POM file of component: " + component.getQualifiedName(), e);
                loadStatus.put(component.getQualifiedName(), 2);
                return 2;
            }
        }

        //try to load from set type
        int componentLoadStatus = 1;
        if (getRepositoryType(component) != null) {
            componentLoadStatus = loadComponent(component, getRepositoryType(component));
        }

        if (componentLoadStatus != 0) {
            for (var type : MavenComponentRepositoryType.values()) {
                if (type == MavenComponentRepositoryType.ROOT || type == MavenComponentRepositoryType.CUSTOM) continue;

                if (type == getRepositoryType(component)) continue;

                var newComponentLoadStatus = loadComponent(component, type);
                if (newComponentLoadStatus > componentLoadStatus) {
                    componentLoadStatus = newComponentLoadStatus;
                }
                if (newComponentLoadStatus == 0)
                    componentLoadStatus = 0;
                if (componentLoadStatus == 0) {
                    types.put(component, type);
                    break;
                }

            }
        }

        switch (componentLoadStatus) {
            case 0 ->
                    logger.success("Loaded data for component " + component.getQualifiedName() + ". (" + (System.currentTimeMillis() - start) + "ms)");
            case 1 ->
                    logger.error("Could not load component: " + component.getQualifiedName() + ". [not found in repository] (" + (System.currentTimeMillis() - start) + "ms)");
            case 2 ->
                    logger.error("Could not load component " + component.getQualifiedName() + ". [error parsing model] (" + (System.currentTimeMillis() - start) + "ms)");
        }

        loadStatus.put(component.getQualifiedName(), componentLoadStatus);
        return componentLoadStatus;
    }

    private int loadComponent(Component component, MavenComponentRepositoryType type) {
        if (type == MavenComponentRepositoryType.ROOT) {
            return 1;
        }

        var cacheDir = getCacheDirectory(component);
        try {
            if (cacheDir != null && isComponentCached(cacheDir)) {
                logger.info("Loading " + component.getQualifiedName() + " from cache");
                loadFromCache(component, cacheDir);
                loadStatus.put(component.getQualifiedName(), 0);
                return 0;
            }
        } catch (XMLStreamException | IOException e) {
            logger.error("Could not load component from cache: " + component.getQualifiedName(), e);
        }

        try {
            logger.info("Loading for " + component.getQualifiedName() + " from repository type " + type);
            var downloadLocation = getDownloadLocation(component, type);
            component.setData("model", loadModel(downloadLocation + ".pom"));
            component.setData("hashes", loadHashes(downloadLocation + ".jar"));
            loadStatus.put(component.getQualifiedName(), 0);
            saveToCache(cacheDir, downloadLocation);
            return 0;
        } catch (IOException e) {
            return 1;
        } catch (XMLStreamException e) {
            logger.info("Could not parse POM file of component: " + component.getQualifiedName() + " in MavenRepository with type " + type + ". Trying other Repositories.");
            loadStatus.put(component.getQualifiedName(), 2);
            return 2;
        }
    }

    private Path getCacheDirectory(Component component) {
        if (Settings.getDataFolder() == null) return null;
        return Paths.get(Settings.getDataFolder().getAbsolutePath() + "/maven/", component.getGroup().replace(".", "/"), component.getArtifactId(), component.getVersion().version());
    }

    private boolean isComponentCached(Path cacheDir) {
        return Files.exists(cacheDir.resolve("model.pom"));
    }

    private void loadFromCache(Component component, Path cacheDir) throws IOException, XMLStreamException {
        component.setData("model", loadModelFromPom(cacheDir.resolve("model.pom").toString()));
        component.setData("hashes", loadHashesFromCache(cacheDir));
    }

    private List<Hash> loadHashesFromCache(Path cacheDir) throws IOException {
        List<Hash> hashes = new ArrayList<>();
        for (String algorithm : new String[]{"md5", "sha1", "sha256", "sha512"}) {
            Path hashFile = cacheDir.resolve("hash." + algorithm);
            if (Files.exists(hashFile)) {
                String value = Files.readString(hashFile).trim();
                hashes.add(Hash.of(algorithm, value));
            }
        }
        return hashes;
    }

    private Model loadModelFromPom(String filePath) throws IOException, XMLStreamException {
        MavenStaxReader reader = new MavenStaxReader();
        Model model;
        try (var inputStream = new FileReader(filePath)) {
            model = reader.read(inputStream);
        }
        return model;
    }

    private Model loadModel(String url) throws XMLStreamException, IOException {

        MavenStaxReader reader = new MavenStaxReader();
        Model model;
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            model = reader.read(inputStream);
        }
        return model;
    }

    private List<Hash> loadHashes(String baseUrl) {
        var hashes = new ArrayList<Hash>();
        for (var algorithm : new String[]{"md5", "sha1", "sha256", "sha512"}) {
            try {
                hashes.add(loadHash(baseUrl, algorithm));
            } catch (FileNotFoundException ignored) {
            } catch (IOException e) {
                logger.error("Error getting hash", e);
            }
        }
        return hashes;
    }

    private void saveToCache(Path cacheDir, String downloadLocation) throws IOException {
        Files.createDirectories(cacheDir);
        saveModelToCache(cacheDir, downloadLocation + ".pom");
        saveHashesToCache(cacheDir, downloadLocation + ".jar");
    }

    private void saveModelToCache(Path cacheDir, String url) throws IOException {
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            Files.copy(inputStream, cacheDir.resolve("model.pom"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void saveHashesToCache(Path cacheDir, String baseUrl) throws IOException {
        for (String algorithm : new String[]{"md5", "sha1", "sha256", "sha512"}) {
            try (InputStream inputStream = URI.create(baseUrl + "." + algorithm).toURL().openStream()) {
                Files.copy(inputStream, cacheDir.resolve("hash." + algorithm));
            } catch (FileNotFoundException ignored) {
            }
        }
    }

    private Hash loadHash(String baseUrl, String algorithm) throws IOException {
        try (InputStream inputStream = URI.create(baseUrl + "." + algorithm).toURL().openStream()) {
            var value = new String(inputStream.readAllBytes());
            // some files have some spaces and a - at the end. we dont want that
            if (value.contains(" ")) value = value.substring(0, value.indexOf(" "));
            return Hash.of(algorithm, value);
        }
    }

    @Override
    public synchronized Component getComponent(String groupId, String artifactId, Version version, Component parent) {
        var key = groupId + ":" + artifactId;

        if (components.containsKey(key)) {
            var available = components.get(key).stream().filter(it -> it.getVersion().equals(version)).findFirst();
            if (available.isPresent())
                return available.get();
        }

        var newComponent = new MavenComponent(groupId, artifactId, version);
        if (getRepositoryType(parent) != null) types.put(newComponent, getRepositoryType(parent));
        else types.put(newComponent, MavenComponentRepositoryType.Central);


        components.computeIfAbsent(key, k -> new TreeSet<>(Comparator.comparing(Component::getVersion))).add(newComponent);
        return newComponent;
    }

    @Override
    public String getDownloadLocation(Component component) {
        if (getRepositoryType(component) == MavenComponentRepositoryType.CUSTOM) {
            return null;
        }
        return getDownloadLocation(component, getRepositoryType(component));
    }

    private String getDownloadLocation(Component component, MavenComponentRepositoryType type) {
        return type.getUrl() + component.getGroup().replace(".", "/") + "/" + component.getArtifactId() + "/" + component.getVersion().version() + "/" + component.getArtifactId() + "-" + component.getVersion().version();
    }

    @Override
    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        if (components.get(groupName + ":" + artifactName) == null) {
            return List.of();
        }
        return new ArrayList<>(components.get(groupName + ":" + artifactName));
    }

    @Override
    public List<Component> getLoadedComponents() {
        return this.components.values().stream().flatMap(TreeSet::stream).toList();
    }

}
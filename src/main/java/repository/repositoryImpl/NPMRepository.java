package repository.repositoryImpl;

import data.Component;
import data.Dependency;
import data.Version;
import logger.Logger;
import repository.ComponentRepository;
import service.VersionResolver;
import settings.Settings;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class NPMRepository implements ComponentRepository {
    private final static NPMRepository instance = new NPMRepository();
    Logger logger = Logger.of("NPMRepository");

    String baseURL = "https://registry.npmjs.org/";
    File cacheDir;
    HashMap<String, TreeMap<Version, Component>> loadedComponents = new HashMap<>();

    private NPMRepository() {
        cacheDir = new File(Settings.getDataFolder(), "npm");
        if (!cacheDir.mkdir()) {
            logger.error("Could not create directory " + cacheDir.getAbsolutePath() + ". Caching will not be used.");
        }
    }

    public static NPMRepository getInstance() {
        return instance;
    }

    @Override
    public List<Version> getVersions(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionResolver getVersionResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int loadComponent(Component component) {
        return 0;
    }

    @Override
    public synchronized Component getComponent(@Nullable String groupId, String artifactId, Version version, Component parent) {
        if (loadedComponents.containsKey(artifactId)) {
            var versions = loadedComponents.get(artifactId);
            if (versions.containsKey(version)) {
                return versions.get(version);
            }
        }

//        var newComponent = new NPMComponent

        return null;
    }

    @Override
    public String getDownloadLocation(Component component) {
        return "";
    }

    @Override
    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        return List.of();
    }

    @Override
    public List<Component> getLoadedComponents() {
        return List.of();
    }
}

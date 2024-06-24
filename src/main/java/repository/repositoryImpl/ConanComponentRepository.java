package repository.repositoryImpl;

import com.google.gson.JsonParser;
import data.Component;
import data.Dependency;
import data.Version;
import data.internalData.ConanComponent;
import exceptions.ArtifactBuilderException;
import logger.Logger;
import repository.ComponentRepository;
import service.VersionResolver;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class ConanComponentRepository implements ComponentRepository {
    private static final Logger logger = Logger.of("ConanRepository");
    private static final ConanComponentRepository instance = new ConanComponentRepository();
    HashMap<String, TreeSet<Component>> components = new HashMap<>();

    private ConanComponentRepository() {
    }

    public static ConanComponentRepository getInstance() {
        return instance;
    }

    @Override
    public List<? extends Version> getVersions(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionResolver getVersionResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int loadComponent(Component component) {
        try {
            var start = System.currentTimeMillis();
            logger.info("Loading conan component: " + component.getQualifiedName());

            //TODO this link doesnt look stable :/
            var url = URI.create("https://conan.io/_next/data/eh_CQjRsfRlA2uGc_doGF/center/recipes/" + component.getArtifactId() + ".json?version=" + component.getVersion().version()).toURL();

            var data = JsonParser.parseReader(new InputStreamReader(url.openStream()));

            if (data.isJsonNull()) throw new ArtifactBuilderException("Cannot find artifact at " + url);
            var possibleArtifacts = data.getAsJsonObject().get("pageProps").getAsJsonObject().get("data").getAsJsonObject();
            if (!possibleArtifacts.has("0")) throw new ArtifactBuilderException("Cannot find artifact at " + url);

            var foundVersions = new ArrayList<String>();
            for (int i = 0; i < possibleArtifacts.size(); i++) {
                var possibleArtifact = possibleArtifacts.get(Integer.toString(i)).getAsJsonObject();
                var name = possibleArtifact.get("name").getAsString();
                if (!name.equals(component.getArtifactId())) continue;
                var version = possibleArtifact.get("info").getAsJsonObject().get("version").getAsString();

                if (version.equals(component.getVersion().version())) {
                    component.setData("jsonData", possibleArtifact.get("info").getAsJsonObject());
                    logger.success("Loaded component: " + component.getQualifiedName() + "(" + (System.currentTimeMillis() - start) + "ms)");
                    return 0;
                } else {
                    foundVersions.add(version);
                }
            }

            throw new ArtifactBuilderException("Could not find version " + component.getVersion().version() + ". Found versions: " + foundVersions);

        } catch (Exception e) {
            logger.error("Could not load component " + component.getQualifiedName(), e);
            return 1;
        }
    }

    @Override
    public synchronized Component getComponent(String ignored, String name, Version version, Component parent) {
        if (components.containsKey(name)) {
            var available = components.get(name).stream().filter(it -> it.getVersion().equals(version)).findFirst();
            if (available.isPresent())
                return available.get();
        }
        var newComponent = new ConanComponent(name, version);

        components.computeIfAbsent(name, k -> new TreeSet<>(Comparator.comparing(Component::getVersion))).add(newComponent);
        return newComponent;
    }

    @Override
    public String getDownloadLocation(Component component) {
        return "";
    }

    @Override
    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        return new ArrayList<>(components.get(artifactName));
    }
}

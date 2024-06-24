package repository.repositoryImpl;

import data.Component;
import data.Dependency;
import data.LicenseChoice;
import data.Version;
import data.internalData.AndroidNativeComponent;
import logger.Logger;
import repository.ComponentRepository;
import repository.LicenseRepository;
import service.VersionResolver;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class AndroidNativeComponentRepository implements ComponentRepository {
    private static final Logger logger = Logger.of("AndroidN_Repository");
    private static final AndroidNativeComponentRepository instance = new AndroidNativeComponentRepository();
    private static final String baseUrl = "https://android.googlesource.com/";
    HashMap<String, TreeSet<Component>> components = new HashMap<>();

    private AndroidNativeComponentRepository() {
    }

    public static AndroidNativeComponentRepository getInstance() {
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
        var start = System.currentTimeMillis();
        logger.info("Loading Android Native Component: " + component.getQualifiedName());

        var license = loadLicense(getDownloadLocation(component) + "/LICENSE?format=TEXT");
        var owners = loadOwners(getDownloadLocation(component) + "/OWNERS?format=TEXT");
        component.setData("license", license);
        component.setData("owners", owners);

        if (license != null && owners != null) {
            logger.success("Loaded Android Native Component: " + component.getQualifiedName() + "(" + (System.currentTimeMillis() - start) + "ms)");
            return 0;
        }
        return 1;
    }

    private List<String> loadOwners(String url) {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            byte[] base64Bytes = in.readAllBytes();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Bytes);
            String decodedContent = new String(decodedBytes, StandardCharsets.UTF_8);
            logger.info("Loaded owners for AndroidNative from " + url);
            return List.of(decodedContent.split("\n"));
        } catch (Exception e) {
            logger.error("Could not load owners for AndroidNative from " + url, e);
        }
        return null;
    }

    private LicenseChoice loadLicense(String url) {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            byte[] base64Bytes = in.readAllBytes();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Bytes);
            String decodedContent = new String(decodedBytes, StandardCharsets.UTF_8);
            var res = LicenseRepository.getInstance().getLicense(decodedContent, url);
            logger.info("Loaded license for AndroidNative from " + url);
            return LicenseChoice.of(res, null, null);
        } catch (Exception e) {
            logger.error("Could not load license for AndroidNative from " + url, e);
        }
        return null;
    }

    @Override
    public synchronized Component getComponent(String groupId, String artifactId, Version version, Component parent) {
        var key = groupId + ":" + artifactId;

        if (components.containsKey(key)) {
            var available = components.get(key).stream().filter(it -> it.getVersion().equals(version)).findFirst();
            if (available.isPresent())
                return available.get();
        }

        var newComponent = new AndroidNativeComponent(groupId, artifactId, version);
        components.computeIfAbsent(key, k -> new TreeSet<>(Comparator.comparing(Component::getVersion))).add(newComponent);
        return newComponent;
    }

    @Override
    public String getDownloadLocation(Component component) {
        return baseUrl + component.getGroup().replace(".", "/") + "/" + component.getArtifactId() + "/+/refs/heads/" + component.getVersion().version();
    }

    @Override
    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        return List.of();
    }
}

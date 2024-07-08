package repository.repositoryImpl;

import data.Component;
import data.Dependency;
import data.LicenseChoice;
import data.Version;
import data.internalData.JitPackComponent;
import logger.Logger;
import repository.ComponentRepository;
import repository.LicenseRepository;
import service.VersionResolver;
import util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipFile;

public class JitPackComponentRepository implements ComponentRepository {
    private static final Logger logger = Logger.of("JitPackRepository");
    private static final String baseurl = "https://github.com/";
    private static final File tempZipFolder = new File(Constants.getDataFolder() + "/jitpack/");
    private static final JitPackComponentRepository instance = new JitPackComponentRepository();
    HashMap<String, TreeSet<Component>> components = new HashMap<>();

    private JitPackComponentRepository() {
    }

    public static JitPackComponentRepository getInstance() {
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
        logger.info("Loading JitPack Component: " + component.getQualifiedName());

        if (!tempZipFolder.exists())
            if (!tempZipFolder.mkdirs()) {
                logger.error("Could not create a download folder " + tempZipFolder.getAbsoluteFile() + ". Cannot load JitPackComponent " + component);
                return 2;
            }

        var zipFileF = new File(tempZipFolder + "/" + component.getGroup() + "_" + component.getArtifactId() + "_" + component.getVersion().version() + ".zip");
        if (downloadZipFile(component, zipFileF)) {
            try (ZipFile zipFile = new ZipFile(zipFileF)) {
                var entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.getName().toLowerCase().contains("license")){
                        var licenseData = new String(zipFile.getInputStream(entry).readAllBytes());
                        var license = LicenseRepository.getInstance().getLicense(licenseData, getDownloadLocation(component));
                        component.setData("licenseChoice", LicenseChoice.of(license, null, null));
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Could not parse JitPack Component " + component.getQualifiedName() + ". Probably no LICENSE file present in repository. ", e);
                return 2;
            }
            logger.success("Loaded JitPack Component: " + component.getQualifiedName() + "(" + (System.currentTimeMillis() - start) + "ms)");
            return 0;
        } else {
            return 1;
        }
    }

    private boolean downloadZipFile(Component component, File zipFile) {
        var group = component.getGroup();
        if (group.startsWith("com.github")) {
            group = group.substring(10);
        }

        try (InputStream in = URI.create(baseurl + group + "/" + component.getArtifactId() + "/archive/refs/tags/" + component.getVersion().version() + ".zip").toURL().openStream();
             FileOutputStream zipOutStream = new FileOutputStream(zipFile)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                zipOutStream.write(dataBuffer, 0, bytesRead);
            }

        } catch (IOException e) {
            logger.error("Could not load JitPack component " + component.getQualifiedName(), e);
            return false;
        }
        return true;
    }

    @Override
    public synchronized Component getComponent(String groupId, String artifactId, Version version, Component parent) {
        var key = groupId + ":" + artifactId;

        if (components.containsKey(key)) {
            var available = components.get(key).stream().filter(it -> it.getVersion().equals(version)).findFirst();
            if (available.isPresent())
                return available.get();
        }

        var newComponent = new JitPackComponent(groupId, artifactId, version);

        components.computeIfAbsent(key, k -> new TreeSet<>(Comparator.comparing(Component::getVersion))).add(newComponent);
        return newComponent;
    }

    @Override
    public String getDownloadLocation(Component component) {
        return baseurl + component.getGroup() + "/" + component.getArtifactId() + "/releases/tag/" + component.getVersion().version();
    }

    @Override
    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        return List.of();
    }

    @Override
    public List<Component> getLoadedComponents() {
        return this.components.values().stream().flatMap(TreeSet::stream).toList();
    }
}

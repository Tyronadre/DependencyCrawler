package repository.repositoryImpl;

import cyclonedx.sbom.Bom16;
import data.Component;
import data.Dependency;
import data.ReadComponent;
import data.Version;
import data.readData.ReadSBomComponent;
import data.readData.ReadSPDXComponent;
import dependencyCrawler.DependencyCrawlerInput;
import org.spdx.library.model.SpdxPackage;
import repository.ComponentRepository;
import service.VersionResolver;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class ReadComponentRepository implements ComponentRepository {
    HashMap<String, Component> readComponents = new HashMap<>();

    private static ReadComponentRepository instance;

    public static ReadComponentRepository getInstance() {
        if (instance == null) {
            instance = new ReadComponentRepository();
        }
        return instance;
    }

    private ReadComponentRepository() {
    }


    private ComponentRepository getRepository(DependencyCrawlerInput.Type type) {
        return switch (type) {
            case MAVEN -> MavenComponentRepository.getInstance();
            case ANDROID_NATIVE -> AndroidNativeComponentRepository.getInstance();
            case CONAN -> ConanComponentRepository.getInstance();
            case JITPACK -> JitPackComponentRepository.getInstance();
            default -> null;
        };
    }

    public ComponentRepository getActualRepository(Component component) {
        if (component instanceof ReadComponent sBomComponent)
            return getRepository(sBomComponent.getType());
        throw new IllegalArgumentException("Cannot get repository of component with type: " + component.getClass().getSimpleName());
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
        return getActualRepository(component).loadComponent(component);
    }

    @Override
    public synchronized Component getComponent(@Nullable String groupId, String artifactId, Version version, Component parent) {
        throw new UnsupportedOperationException("use the specific getComponent method for the type of component you want to get (getSPDXComponent, getSBomComponent, ...)");
    }

    @Override
    public String getDownloadLocation(Component component) {
        return "";
    }

    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        return this.readComponents.values().stream().filter(c -> c.getGroup().equals(groupName) && c.getArtifactId().equals(artifactName)).toList();
    }

    @Override
    public List<Component> getLoadedComponents() {
        return this.readComponents.values().stream().toList();
    }


    //get or find a component by qualifier
    public Component getReadComponent(String qualifier) {
        if (readComponents.containsKey(qualifier))
            return readComponents.get(qualifier);
        return null;
    }


    public synchronized Component getSPDXComponent(SpdxPackage spdxPackage, DependencyCrawlerInput.Type type, String purl) {
        if (readComponents.containsKey(purl))
            return readComponents.get(purl);
        var component = new ReadSPDXComponent(spdxPackage, type, purl);
        readComponents.put(purl, component);
        return component;
    }

    public synchronized Component getSBomComponent(Bom16.Component bomComponent, DependencyCrawlerInput.Type type, String purl) {
        if (readComponents.containsKey(bomComponent.getPurl()))
            return readComponents.get(bomComponent.getPurl());
        var component = new ReadSBomComponent(bomComponent, type, purl);
        readComponents.put(bomComponent.getBomRef(), component);
        return component;
    }
}

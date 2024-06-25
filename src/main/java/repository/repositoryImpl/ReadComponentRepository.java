package repository.repositoryImpl;

import cyclonedx.sbom.Bom16;
import data.Component;
import data.Dependency;
import data.Version;
import data.readData.ReadSBomComponent;
import repository.ComponentRepository;
import service.VersionResolver;

import java.util.HashMap;
import java.util.List;

public class ReadComponentRepository implements ComponentRepository {
    HashMap<String, ReadSBomComponent> readComponents = new HashMap<>();

    private static ReadComponentRepository instance;

    public static ReadComponentRepository getInstance() {
        if (instance == null) {
            instance = new ReadComponentRepository();
        }
        return instance;
    }

    private ReadComponentRepository() {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Component getComponent(String groupId, String artifactId, Version version, Component parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDownloadLocation(Component component) {
        return "";
    }

    public List<Component> getLoadedComponents(String groupName, String artifactName) {
        return this.readComponents.values().stream().filter(c -> c.getGroup().equals(groupName) && c.getArtifactId().equals(artifactName)).map(i -> (Component) i).toList();
    }

    public void addReadComponent(Bom16.Component bomComponent, ReadSBomComponent component) {
        this.readComponents.put(bomComponent.getBomRef(), component);
    }

    public Component getReadComponent(Bom16.Component bomComponent) {
        return this.readComponents.get(bomComponent.getBomRef());
    }

    //get or find a component by qualifier
    public ReadSBomComponent getReadComponent(String qualifier) {
        if (readComponents.containsKey(qualifier))
            return readComponents.get(qualifier);
        for (var e : readComponents.entrySet()) {
            if (e.getValue().getQualifiedName().contains(qualifier))
                return e.getValue();
        }
        return null;
    }
}

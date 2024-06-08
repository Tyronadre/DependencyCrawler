package repository.repositoryImpl;

import cyclonedx.sbom.Bom16;
import data.Component;
import data.Dependency;
import data.Version;
import enums.RepositoryType;
import repository.ComponentRepository;
import service.VersionRangeResolver;
import service.VersionResolver;

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


    @Override
    public List<? extends Version> getVersions(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionResolver getVersionResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionRangeResolver getVersionRangeResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean loadComponent(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component getComponent(String groupId, String artifactId, Version version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RepositoryType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDownloadLocation(Component component) {
        return "";
    }

    public void addReadComponent(Bom16.Component bomComponent, Component component) {
        this.readComponents.put(bomComponent.getBomRef(), component);
    }

    public Component getReadComponent(Bom16.Component bomComponent) {
        return this.readComponents.get(bomComponent.getBomRef());
    }

    public List<Component> getAllComponents() {
        return readComponents.values().stream().toList();
    }

    public Component getReadComponent(String qualifier) {
        return readComponents.get(qualifier);
    }
}

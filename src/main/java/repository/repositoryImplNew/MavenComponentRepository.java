package repository.repositoryImplNew;

import data.Version;
import dataNew.Component;
import dataNew.Dependency;

import javax.annotation.Nullable;
import java.util.List;

public class MavenComponentRepository implements NewComponentRepository {

    @Override
    public Version resolveVersion(Dependency dependency) {
        return null;
    }

    @Override
    public LoadingStatus loadComponent(dataNew.Component component) {
        return null;
    }

    @Override
    public dataNew.Component getComponent(@Nullable String groupId, String artifactId, Version version, dataNew.Component parent) {
        return null;
    }

    @Override
    public String getDownloadLocation(dataNew.Component component) {
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

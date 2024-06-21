package repository.repositoryImpl;

import com.google.gson.JsonParser;
import data.Component;
import data.Dependency;
import data.Version;
import data.internalData.ConanComponent;
import enums.RepositoryType;
import repository.ComponentRepository;
import service.VersionRangeResolver;
import service.VersionResolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

public class ConanRepository implements ComponentRepository {
    private static final ConanRepository instance = new ConanRepository();
    public static ConanRepository getInstance() {
        return instance;
    }

    private ConanRepository(){}

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
    public int loadComponent(Component component) {
        try {
            var url = URI.create("https://conan.io/api/search/" + component.getName() + "%2520" + component.getVersion().getVersion()).toURL();

            var data = JsonParser.parseReader(new InputStreamReader(url.openStream()));

            System.out.println(data);

        } catch (Exception e) {
            return 1;
        }
        return 0;
    }

    @Override
    public Component getComponent(String ignored, String name, Version version) {
        return new ConanComponent( name, version);
    }

    @Override
    public String getDownloadLocation(Component component) {
        return "";
    }
}

package data.internalData;

import com.google.gson.JsonObject;
import data.Component;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import data.LicenseChoice;
import data.Organization;
import data.Person;
import data.Property;
import data.Version;
import data.Vulnerability;
import repository.ComponentRepository;
import repository.LicenseRepository;
import repository.repositoryImpl.ConanComponentRepository;

import java.util.ArrayList;
import java.util.List;

public class ConanComponent implements Component {
    String name;
    Version version;
    boolean loaded = false;
    List<Dependency> dependencies = new ArrayList<>();
    List<LicenseChoice> licenseChoices = new ArrayList<>();
    JsonObject jsonData = null;

    public ConanComponent(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public void loadComponent() {
        if (!loaded && ConanComponentRepository.getInstance().loadComponent(this) == 0) {


            //DEPENDENCIES
            if (this.jsonData.get("use_it") != null)
                for (var dependencyO : this.jsonData.get("use_it").getAsJsonObject().get("requires").getAsJsonArray()) {
                    var dSplit = dependencyO.getAsString().split("/");
                    this.dependencies.add(new ConanDependency(dSplit[0], Version.of(dSplit[1]), this));
                }


            //LICENSE
            if (this.jsonData.get("licenses") != null)
                for (var licenseES : this.jsonData.get("licenses").getAsJsonObject().entrySet())
                    this.licenseChoices.add(LicenseChoice.of(LicenseRepository.getInstance().getLicense(licenseES.getKey(), null), null, null));

            this.loaded = true;

        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public List<Dependency> getDependenciesFiltered() {
        return dependencies;
    }

    @Override
    public String getQualifiedName() {
        return name + ":" + version.version();
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getArtifactId() {
        return name;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Organization getSupplier() {
        return null;
    }

    @Override
    public Organization getManufacturer() {
        return null;
    }

    @Override
    public List<Person> getContributors() {
        return null;
    }

    @Override
    public String getDescription() {
        return jsonData == null ? null : jsonData.get("description").getAsString();
    }

    @Override
    public ComponentRepository getRepository() {
        return ConanComponentRepository.getInstance();
    }

    @Override
    public String getPurl() {
        return "pkg:conan/" + this.getArtifactId() + "@" + this.getVersion().version();
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Component getParent() {
        return null;
    }

    @Override
    public void addDependency(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRoot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ExternalReference> getAllExternalReferences() {
        return List.of(ExternalReference.of("EXTERNAL_REFERENCE_TYPE_WEBSITE", jsonData.get("homepage").getAsString(), null, null));
    }

    @Override
    public List<Hash> getAllHashes() {
        return List.of();
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return List.of();
    }

    @Override
    public String getDownloadLocation() {
        return jsonData.get("homepage").getAsString();
    }

    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return licenseChoices;
    }

    @Override
    public List<Property> getAllProperties() {
        return null;
    }

    @Override
    public List<Person> getAllAuthors() {
        return null;
    }

    @Override
    public <T> void setData(String key, T value) {
        if (key.equals("jsonData")) {
            this.jsonData = (JsonObject) value;
        }
    }

    @Override
    public void removeDependency(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeVulnerability(Vulnerability vulnerability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addVulnerability(Vulnerability vulnerability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.getPurl();
    }
}

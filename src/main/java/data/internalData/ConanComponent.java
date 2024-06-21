package data.internalData;

import data.*;
import repository.ComponentRepository;
import repository.repositoryImpl.ConanRepository;

import java.util.List;

public class ConanComponent implements Component {
    String name ;
    Version version;
    boolean loaded = false;
    List<Dependency> dependencies;
    List<LicenseChoice> licenseChoices;

    public ConanComponent(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public void loadComponent() {
        if (!loaded && ConanRepository.getInstance().loadComponent(this) == 0) {
            this.loaded = true;
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public List<Dependency> getDependencies() {
        return List.of();
    }

    @Override
    public List<Dependency> getDependenciesFiltered() {
        return List.of();
    }

    @Override
    public String getQualifiedName() {
        return "";
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Version getVersion() {
        return null;
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
        return List.of();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ComponentRepository getRepository() {
        return null;
    }

    @Override
    public String getPurl() {
        return "";
    }

    @Override
    public String getProperty(String key) {
        return "";
    }

    @Override
    public Component getParent() {
        return null;
    }

    @Override
    public void addDependency(Dependency dependency) {

    }

    @Override
    public void setRoot() {

    }

    @Override
    public List<ExternalReference> getAllExternalReferences() {
        return List.of();
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
        return "";
    }

    @Override
    public List<Dependency> getDependenciesFlatFiltered() {
        return List.of();
    }

    @Override
    public List<Component> getDependencyComponentsFlatFiltered() {
        return List.of();
    }

    @Override
    public String getPublisher() {
        return "";
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return List.of();
    }

    @Override
    public List<Property> getAllProperties() {
        return List.of();
    }

    @Override
    public List<Person> getAllAuthors() {
        return List.of();
    }

    @Override
    public <T> void setData(String key, T value) {

    }

    @Override
    public void removeDependency(Dependency dependency) {

    }

    @Override
    public void removeVulnerability(Vulnerability vulnerability) {

    }

    @Override
    public void addVulnerability(Vulnerability vulnerability) {

    }
}

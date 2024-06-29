package data.readData;

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
import repository.repositoryImpl.VulnerabilityRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class ReadVexComponent implements Component {
    private final List<Vulnerability> vulnerabilities;
    private boolean loaded;
    private final String group;
    private final String name;
    private final Version version;

    public ReadVexComponent(String group, String name, Version version) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.loaded = false;
        this.vulnerabilities = new ArrayList<>();
    }

    @Override
    public synchronized void loadComponent() {
        VulnerabilityRepositoryImpl.getInstance().updateVulnerabilities(this);

        loaded = true;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public List<Dependency> getDependencies() {
        return null;
    }

    @Override
    public List<Dependency> getDependenciesFiltered() {
        return null;
    }

    @Override
    public String getQualifiedName() {
        return group + ":" + name + ":" + version;
    }

    @Override
    public String getGroup() {
        return group;
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
        return null;
    }

    @Override
    public ComponentRepository getRepository() {
        return null;
    }

    @Override
    public String getPurl() {
        return null;
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

    }

    @Override
    public void setRoot() {

    }

    @Override
    public List<ExternalReference> getAllExternalReferences() {
        return null;
    }

    @Override
    public List<Hash> getAllHashes() {
        return null;
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return vulnerabilities;
    }

    @Override
    public String getDownloadLocation() {
        return null;
    }

    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return null;
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
        if (key.equals("addVulnerability")) {
            this.vulnerabilities.add((Vulnerability) value);
        }

    }

    @Override
    public void removeDependency(Dependency dependency) {

    }

    @Override
    public void removeVulnerability(Vulnerability vulnerability) {
        this.vulnerabilities.remove(vulnerability);
    }

    @Override
    public void addVulnerability(Vulnerability vulnerability) {
        this.vulnerabilities.add(vulnerability);
    }


}

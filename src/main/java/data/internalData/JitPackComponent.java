package data.internalData;

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
import repository.repositoryImpl.JitPackComponentRepository;

import java.util.List;

public class JitPackComponent implements Component {
    private static final JitPackComponentRepository repository = JitPackComponentRepository.getInstance();
    String groupId;
    String artifactId;
    Version version;
    Boolean isLoaded = false;
    LicenseChoice licenseChoice = null;

    public JitPackComponent(String groupId, String artifactId, Version version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public void loadComponent() {
        if (!isLoaded) {
            repository.loadComponent(this);
            this.isLoaded = true;
        }
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public List<Dependency> getDependencies() {
        return List.of();
    }


    @Override
    public String getQualifiedName() {
        return this.groupId + ":" + this.artifactId + ":" + this.version.version();
    }

    @Override
    public String getGroup() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
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
        return List.of();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ComponentRepository getRepository() {
        return repository;
    }

    @Override
    public String getPurl() {
        return "pkg:jitpack/" + groupId + "/" + artifactId + "@" + version.version();
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
        return repository.getDownloadLocation(this);
    }

    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        if (licenseChoice == null) return List.of();
        return List.of(licenseChoice);
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
        switch (key) {
            case "licenseChoice" -> this.licenseChoice = (LicenseChoice) value;
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

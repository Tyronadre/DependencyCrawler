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
import repository.repositoryImpl.AndroidNativeComponentRepository;

import java.util.List;

public class AndroidNativeComponent implements Component {
    String groupId;
    String artifactId;
    Version version;
    Boolean loaded = false;
    List<String> owners;
    LicenseChoice licenseChoice;
    Organization google = Organization.of("Google, AndroidNative", List.of("https://developer.android.com/ndk"), null, null);

    public AndroidNativeComponent(String groupId, String artifactId, Version version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public void loadComponent() {
        if (isLoaded()) return;

        if (AndroidNativeComponentRepository.getInstance().loadComponent(this) == 0)
            loaded = true;
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
    public String getQualifiedName() {
        return groupId + ":" + artifactId + ":" + version.version();
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
        return google;
    }

    @Override
    public Organization getManufacturer() {
        return google;
    }

    @Override
    public List<Person> getContributors() {
        if (owners == null) return List.of();
        return owners.stream().map(it -> Person.of(it, null, null, null, google, null)).toList();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ComponentRepository getRepository() {
        return AndroidNativeComponentRepository.getInstance();
    }

    @Override
    public String getPurl() {
        return "pkg:android_native/" + groupId + "/" + artifactId + "@" + version.version();
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
        return getRepository().getDownloadLocation(this);
    }

    @Override
    public String getPublisher() {
        return "google";
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
        return this.getContributors();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void setData(String key, T value) {
        switch (key) {
            case "owners" -> this.owners = (List<String>) value;
            case "license" -> this.licenseChoice = (LicenseChoice) value;
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

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
import logger.Logger;
import org.apache.maven.api.model.DependencyManagement;
import org.apache.maven.api.model.Model;
import repository.ComponentRepository;
import repository.LicenseRepository;
import repository.VulnerabilityRepository;
import repository.repositoryImpl.MavenComponentRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An artifact in a Maven repository.
 */
public class MavenComponent implements Component {
    private static final Logger logger = Logger.of("MavenComponent");

    String groupId;
    String artifactId;
    Version version;
    List<Dependency> dependencies = new ArrayList<>();
    ComponentRepository repository = MavenComponentRepository.getInstance();
    Model model;
    Component parent;
    List<Hash> hashes = new ArrayList<>();
    boolean loaded = false;
    boolean isRoot = false;
    private List<Vulnerability> vulnerabilities;
    private List<LicenseChoice> licenseChoices = new ArrayList<>();
    private List<Person> authors;

    public MavenComponent(String groupId, String artifactId, Version version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    @Override
    public List<Dependency> getDependenciesFiltered() {
        return this.dependencies.stream()
                .filter(Objects::nonNull)
                .filter(Dependency::shouldResolveByScope)
                .filter(Dependency::isNotOptional)
                .sorted(Comparator.comparing(Dependency::getQualifiedName))
                .collect(Collectors.toList());
    }

    @Override
    public String getQualifiedName() {
        return groupId.replace(",", ":") + ":" + artifactId + ":" + version.version();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Organization getSupplier() {
        if (this.model == null) return null;
        var org = this.model.getOrganization();
        if (org == null) return null;
        return Organization.of(org.getName(), org.getUrl() != null ? List.of(org.getUrl()) : null, null, null);
    }

    @Override
    public Organization getManufacturer() {
        return this.getSupplier();
    }

    @Override
    public List<Person> getContributors() {
        var l = new ArrayList<Person>();
        if (this.model == null || this.model.getDevelopers() == null) return null;
        for (var developer : model.getDevelopers()) {
            l.add(Person.of(developer.getName(), developer.getEmail(), developer.getUrl(), null, Organization.of(developer.getOrganization(), List.of(developer.getOrganizationUrl()), null, null), developer.getRoles()));
        }
        return l;
    }

    @Override
    public String getDescription() {
        if (this.model == null || this.model.getDescription() == null) return null;
        return this.model.getDescription();
    }

    @Override
    public ComponentRepository getRepository() {
        return this.repository;
    }

    @Override
    public String getPurl() {
        return "pkg:maven/" + groupId + "/" + artifactId + "@" + version.version();
    }

    @Override
    public String getProperty(String key) {
        return this.model.getProperties().get(key);
    }

    @Override
    public Component getParent() {
        return this.parent;
    }

    @Override
    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    @Override
    public void setRoot() {
        this.isRoot = true;
    }


    @Override
    public synchronized void loadComponent() {
        if (loaded) return;

        if (this.isRoot) {
            this.loaded = true;
            return;
        }

        var start = System.currentTimeMillis();

        var loadingState = MavenComponentRepository.getInstance().loadComponent(this);
        if (loadingState != 0) return;


        // DEPENDENCIES
        for (var modelDependency : model.getDependencies()) {
            // special case
            if (modelDependency.getGroupId().equals("${project.groupId}") || modelDependency.getGroupId().equals("${pom.groupId}"))
                this.dependencies.add(new MavenDependency(this.getGroup(), modelDependency.getArtifactId(), modelDependency.getVersion(), modelDependency.getScope(), modelDependency.getOptional(), this));
            else
                this.dependencies.add(new MavenDependency(modelDependency.getGroupId(), modelDependency.getArtifactId(), modelDependency.getVersion(), modelDependency.getScope(), modelDependency.getOptional(), this));
        }

        // PARENT
        if (this.model.getParent() != null) {
            this.parent = this.repository.getComponent(this.model.getParent().getGroupId(), this.model.getParent().getArtifactId(), new VersionImpl(this.model.getParent().getVersion()), null);
        } else {
            this.parent = null;
        }

        // LICENSES
        var licenseRepository = LicenseRepository.getInstance();
        if (this.model.getLicenses() != null && !this.model.getLicenses().isEmpty()) {
            //load from this component
            this.licenseChoices = new ArrayList<>();
            for (var license : this.model.getLicenses()) {
                if (license.getName() == null) continue;
                var newLicense = licenseRepository.getLicense(license.getName(), license.getUrl());
                if (newLicense == null) {
                    logger.error("Could not resolve license for " + this.getQualifiedName() + ": " + license.getName());
                    continue;
                }
                this.licenseChoices.add(new MavenLicenseChoice(newLicense));
            }
        } else {
            //load from maven parent
            if (this.parent == null) return;
            if (!this.parent.isLoaded()) {
                this.parent.loadComponent();
                if (this.parent.isLoaded() && this.parent.getAllLicenses() != null)
                    this.licenseChoices = new ArrayList<>(this.parent.getAllLicenses());
            }
        }

        //PROPERTIES
        for (var entry : this.model.getProperties().entrySet()) {
            this.properties.add(Property.of(entry.getKey(), entry.getValue()));
        }

        //VULNERABILITIES
        this.vulnerabilities = VulnerabilityRepository.getInstance().getVulnerabilities(this);


        loaded = true;
        logger.success("Parsed component: " + this.getQualifiedName() + " (" + (System.currentTimeMillis() - start) + "ms)");

    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String getGroup() {
        return this.groupId;
    }

    @Override
    public String getArtifactId() {
        return this.artifactId;
    }

    @Override
    public String toString() {
        return this.getQualifiedName();
    }

    public DependencyManagement getDependencyManagement() {
        if (this.model == null) {
            this.repository.loadComponent(this);
        }
        return this.model.getDependencyManagement();
    }


    @Override
    public List<ExternalReference> getAllExternalReferences() {
        List<ExternalReference> externalReferences = new ArrayList<>();
        if (this.model == null) return externalReferences;
        if (this.model.getUrl() != null) {
            var externalRef = new ExternalReferenceImpl("homepage", this.model.getUrl());
            externalReferences.add(externalRef);
        }
        if (this.model.getScm() != null) {
            var externalRef = new ExternalReferenceImpl("scm", this.model.getScm().getUrl());
            externalRef.set("connection", this.model.getScm().getConnection());
            externalRef.set("developerConnection", this.model.getScm().getDeveloperConnection());
            externalRef.set("tag", this.model.getScm().getTag());
            externalReferences.add(externalRef);
        }
        if (this.model.getIssueManagement() != null) {
            var externalRef = new ExternalReferenceImpl("issueManagement", this.model.getIssueManagement().getUrl());
            externalRef.set("system", this.model.getIssueManagement().getSystem());
            externalReferences.add(externalRef);
        }
        if (this.model.getCiManagement() != null) {
            var externalRef = new ExternalReferenceImpl("ciManagement", this.model.getCiManagement().getUrl());
            externalRef.set("system", this.model.getCiManagement().getSystem());
            externalReferences.add(externalRef);
        }

        return externalReferences;
    }


    @Override
    public List<Hash> getAllHashes() {
        return hashes;
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return Objects.requireNonNullElseGet(this.vulnerabilities, ArrayList::new);
    }

    @Override
    public String getDownloadLocation() {
        if (this.isRoot) return null;
        return this.repository.getDownloadLocation(this);
    }


    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return this.licenseChoices;
    }

    List<Property> properties = new ArrayList<>();

    @Override
    public List<Property> getAllProperties() {
        return properties;
    }

    @Override
    public List<Person> getAllAuthors() {
        return List.of();
    }

    @Override
    public void removeDependency(Dependency dependency) {
        this.dependencies.remove(dependency);
    }

    @Override
    public void removeVulnerability(Vulnerability vulnerability) {
        this.vulnerabilities.remove(vulnerability);
    }

    @Override
    public void addVulnerability(Vulnerability vulnerability) {
        this.vulnerabilities.add(vulnerability);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setData(String key, Object value) {
        switch (key) {
            case "model" -> setModel((Model) value);
            case "hashes" -> setHashes((List<Hash>) value);
            case "vulnerabilities" -> setVulnerabilities((List<Vulnerability>) value);
            case "repository" -> setRepository((ComponentRepository) value);
            case "addProperty" -> {
                var property = (Property) value;
                this.properties.add(Property.of(property.getName(), property.getValue()));
            }
            case "removeDependencyIfOtherVersionPresent" -> {
                var dependency = (Dependency) value;
                if (this.dependencies.stream().anyMatch(d -> d.getComponent().getGroup().equals(dependency.getComponent().getGroup()) && d.getComponent().getArtifactId().equals(dependency.getComponent().getArtifactId()) && !d.getComponent().getVersion().equals(dependency.getComponent().getVersion()))) {
                    this.dependencies.remove(dependency);
                    logger.info("Removed dependency " + dependency + " from " + this.getQualifiedName() + " because newer version is present.");
                }
            }
            default -> logger.error("Unknown key: " + key);
        }
    }

    private void setRepository(ComponentRepository repository) {
        this.repository = repository;
    }

    private void setModel(Model model) {
        this.model = model;
    }

    private void setHashes(List<Hash> hashes) {
        this.hashes = hashes;
    }

    private void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MavenComponent that)) return false;

        return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }
}

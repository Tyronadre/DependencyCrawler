package data.dataImpl;

import data.*;
import logger.Logger;
import org.apache.maven.api.model.DependencyManagement;
import org.apache.maven.api.model.Model;
import repository.LicenseRepository;
import repository.repositoryImpl.MavenComponentRepository;
import repository.repositoryImpl.MavenRepositoryType;

import java.util.*;

/**
 * An artifact in a Maven repository.
 */
public class MavenComponent implements Component {
    private static final Logger logger = Logger.of("MavenComponent");

    String groupId;
    String artifactId;
    Version version;
    Set<Dependency> dependencies = new HashSet<>();
    MavenComponentRepository repository;
    Model model;
    Component parent;
    List<Hash> hashes = new ArrayList<>();
    boolean loaded = false;
    boolean isRoot = false;
    private List<Vulnerability> vulnerabilities;
    private List<License> licenses;
    private List<Person> authors;

    public MavenComponent(String groupId, String artifactId, Version version, MavenComponentRepository repository) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repository = repository;
    }

    @Override
    public Set<Dependency> getDependencies() {
        //return only dependencies that are not provided or test or optional
        return this.dependencies;
    }

    @Override
    public String getQualifiedName() {
        return groupId.replace(",", ":") + ":" + artifactId + ":" + version.getVersion();
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
        return  Organization.of(org.getName(), List.of(org.getUrl()), null, null);
    }

    @Override
    public Organization getManufacturer() {
        if (this.model == null) return null;
        var org = this.model.getOrganization();
        if (org == null) return null;
        return Organization.of(org.getName(), List.of(org.getUrl()), null, null);
    }

    @Override
    public List<Person> getContributors() {
        var l = new ArrayList<Person>();
        if (this.model == null || this.model.getDevelopers() == null) return null;
        for (var developer : model.getDevelopers()) {
            l.add(Person.of(developer.getName(), developer.getEmail(), developer.getUrl(), null, Organization.of(developer.getOrganization(), List.of(developer.getOrganizationUrl()), null, null),  developer.getRoles()));
        }
        return l;
    }

    @Override
    public String getDescription() {
        if (this.model == null || this.model.getDescription() == null) return null;
        return this.model.getDescription();
    }

    @Override
    public MavenComponentRepository getRepository() {
        return this.repository;
    }

    @Override
    public String getPurl() {
        return "pkg:maven/" + groupId + "/" + artifactId + "@" + version.getVersion();
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
    public void setRepository(MavenComponentRepository mavenRepository) {
        this.repository = mavenRepository;
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
        if (!loaded) {
            if (this.isRoot) {
                this.loaded = true;
                return;
            }

            var start = System.currentTimeMillis();
            logger.info("Loading component: " + this.getQualifiedName());

            if (this.repository != null) this.repository.loadComponent(this);
            //if we dont have a model we try other repositories
            if (model == null) MavenRepositoryType.tryLoadComponent(this);
            //if we still dont have the model, we cant load the component
            if (model == null) {
                logger.error("Could not load component: " + this.getQualifiedName() + " [no model] (" + (System.currentTimeMillis() - start) + "ms)");
                return;
            }

            // DEPENDENCIES
            for (var modelDependency : model.getDependencies()) {
                // special case
                if (modelDependency.getGroupId().equals("${project.groupId}") || modelDependency.getGroupId().equals("${pom.groupId}"))
                    this.dependencies.add(new MavenDependency(this.getGroup(), modelDependency.getArtifactId(), modelDependency.getVersion(), modelDependency.getScope(), modelDependency.getOptional(), this));
                else
                    this.dependencies.add(new MavenDependency(modelDependency.getGroupId(), modelDependency.getArtifactId(), modelDependency.getVersion(), modelDependency.getScope(), modelDependency.getOptional(), this));
            }

            // PARENT
            if (this.model.getParent() != null)
                this.parent = this.repository.getComponent(this.model.getParent().getGroupId(), this.model.getParent().getArtifactId(), new MavenVersion(this.model.getParent().getVersion()));
            else this.parent = null;

            // LICENSES
            var licenseRepository = LicenseRepository.getInstance();
            if (this.model.getLicenses() != null){
                this.licenses = new ArrayList<>();
                for (var license : this.model.getLicenses()) {
                    if (license.getName() == null) continue;
                    var newLicense = licenseRepository.getLicense(license.getName(), license.getUrl());
                    if (newLicense == null) {
                        logger.error("Could not resolve license for " + this.getQualifiedName() + ": " + license.getName());
                        continue;
                    }
                    this.licenses.add(newLicense);
                }

            }

            loaded = true;
            logger.success("Loaded component: " + this.getQualifiedName() + " (" + (System.currentTimeMillis() - start) + "ms)");
        }
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
    public String getName() {
        return this.artifactId;
    }

    public void setModel(Model model) {
        this.model = model;
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

    public String getLicenseExpression() {
        if (this.licenses == null)
            return null;
        var licenses = this.licenses.stream().filter(l -> l instanceof SPDXLicense).toList();
        if (licenses.isEmpty()) {
            return null;
        }

        if (licenses.size() == 1) return licenses.get(0).getId();
        StringBuilder licenseSetString = new StringBuilder("(");
        for (int i = 0; i < licenses.size(); i++) {
            var license = licenses.get(i);
            licenseSetString.append("\"").append(license.getId()).append("\"");
            if (i < licenses.size() - 1) {
                licenseSetString.append(" AND ");
            }
        }
        licenseSetString.append(")");
        return licenseSetString.toString();
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return Objects.requireNonNullElseGet(this.vulnerabilities, ArrayList::new);
    }

    @Override
    public void addVulnerability(Vulnerability vulnerability) {

    }

    @Override
    public String getDownloadLocation() {
        return this.repository.getDownloadLocation(this);
    }

    @Override
    public Set<Dependency> getDependenciesFlat() {
        Set<Dependency> dependencies = new HashSet<>();
        for (var dependency : this.dependencies.stream().filter(Dependency::shouldResolveByScope).filter(Dependency::isNotOptional).toList()) {
            dependencies.add(dependency);
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded())
                dependencies.addAll(dependency.getComponent().getDependenciesFlat());
        }
        return dependencies;
    }

    @Override
    public Set<Component> getDependecyComponentsFlat() {
        Set<Component> components = new HashSet<>();
        for (var dependency : this.dependencies.stream().filter(Dependency::shouldResolveByScope).filter(Dependency::isNotOptional).toList()) {
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded()) {
                components.add(dependency.getComponent());
                components.addAll(dependency.getComponent().getDependecyComponentsFlat());
            }
        }
        return components;
    }

    @Override
    public String getPublisher() {
        return null;
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

    public void setHashes(List<Hash> hashes) {
        this.hashes = hashes;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
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
        int result = Objects.hashCode(groupId);
        result = 31 * result + Objects.hashCode(artifactId);
        result = 31 * result + Objects.hashCode(version);
        return result;
    }
}

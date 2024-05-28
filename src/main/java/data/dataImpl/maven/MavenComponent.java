package data.dataImpl.maven;

import cyclonedx.v1_6.Bom16;
import data.Component;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import data.License;
import data.Organization;
import data.Person;
import data.Version;
import data.Vulnerability;
import data.dataImpl.ExternalReferenceImpl;
import data.dataImpl.OrganizationImpl;
import logger.Logger;
import org.apache.maven.api.model.DependencyManagement;
import org.apache.maven.api.model.Model;
import repository.LicenseRepository;
import repository.repositoryImpl.MavenRepository;
import repository.repositoryImpl.MavenRepositoryType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An artifact in a Maven repository.
 */
public class MavenComponent implements Component {
    Logger logger = Logger.of("Maven");

    String groupId;
    String artifactId;
    Version version;
    String scope;
    List<MavenDependency> dependencies = new ArrayList<>();
    MavenRepository repository;
    Model model;
    Component parent;
    List<Hash> hashes = new ArrayList<>();
    boolean loaded = false;
    boolean isRoot = false;
    private List<Vulnerability> vulnerabilities;
    private List<License> licenses;

    public MavenComponent(String groupId, String artifactId, Version version, MavenRepository repository) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repository = repository;
    }

    @Override
    public List<MavenDependency> getDependencies() {
        //return only dependencies that are not provided or test or optional
        return this.dependencies.stream().toList();
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
        return new OrganizationImpl(org.getName(), org.getUrl());
    }

    @Override
    public Organization getManufacturer() {
        if (this.model == null) return null;
        var org = this.model.getOrganization();
        if (org == null) return null;
        return new OrganizationImpl(org.getName(), org.getUrl());
    }

    @Override
    public List<Person> getContributors() {
        var l = new ArrayList<Person>();
        if (this.model == null || this.model.getDevelopers() == null) return null;
        for (var developer : model.getDevelopers()) {
            l.add(Person.of(developer.getName(), developer.getEmail(), developer.getUrl(), developer.getOrganization(), developer.getOrganizationUrl(), developer.getRoles()));
        }
        return l;
    }

    @Override
    public String getDescription() {
        if (this.model == null || this.model.getDescription() == null) return null;
        return this.model.getDescription();
    }

    @Override
    public MavenRepository getRepository() {
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
    public void setRepository(MavenRepository mavenRepository) {
        this.repository = mavenRepository;
    }

    @Override
    public void addDependency(Dependency dependency) {
        this.dependencies.add((MavenDependency) dependency);
    }

    @Override
    public void setRoot() {
        this.isRoot = true;
    }


    @Override
    public void loadComponent() {
        if (!loaded) {
            if (this.isRoot) {
                this.loaded = true;
                return;
            }

            var start = System.currentTimeMillis();
            logger.infoLine("Loading component: " + this.getQualifiedName());

            if (this.repository != null) this.repository.loadComponent(this);
            //if we dont have a model we try other repositories
            if (model == null) MavenRepositoryType.tryLoadComponent(this);
            //if we still dont have the model, we cant load the component
            if (model == null) {
                logger.errorLine("failed");
                return;
            }

            // DEPENDENCIES
            for (var modelDependency : model.getDependencies()) {
                // special case
                if (modelDependency.getGroupId().equals("${project.groupId}"))
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
                    var newLicense = licenseRepository.getLicense(license.getName());
                    if (newLicense == null) {
                        logger.errorLine("Could not resolve license for " + this.getQualifiedName());
                        continue;
                    }
                    this.licenses.add(newLicense);
                }

            }

            loaded = true;
            logger.successLine("Loaded component: " + this.getQualifiedName() + " (" + (System.currentTimeMillis() - start) + "ms)");
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

    @Override
    public String getLicenseExpression() {
        if (this.licenses == null || this.licenses.isEmpty())
            return null;
        if (this.licenses.size() == 1) return this.licenses.get(0).getId();
        StringBuilder licenseSetString = new StringBuilder("(");
        for (int i = 0; i < this.licenses.size(); i++) {
            var license = this.licenses.get(i);
            licenseSetString.append("\"").append(license.getId()).append("\"");
            if (i < this.licenses.size() - 1) {
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
    public String getDownloadLocation() {
        return this.repository.getDownloadLocation(this);
    }

    @Override
    public List<Dependency> getDependenciesFlat() {
        List<Dependency> dependencies = new ArrayList<>();
        for (var dependency : this.dependencies.stream().filter(MavenDependency::shouldResolveByScope).filter(MavenDependency::isNotOptional).toList()) {
            dependencies.add(dependency);
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded())
                dependencies.addAll(dependency.getComponent().getDependenciesFlat());
        }
        return dependencies;
    }

    public void setHashes(List<Hash> hashes) {
        this.hashes = hashes;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    @Override
    public Bom16.Component toBom16() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getBomRef() {
        return this.getQualifiedName();
    }
}

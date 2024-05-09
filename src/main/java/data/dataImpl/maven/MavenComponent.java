package data.dataImpl.maven;

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
import data.dataImpl.HashImpl;
import data.dataImpl.LicenseImpl;
import data.dataImpl.OrganizationImpl;
import org.apache.maven.api.model.DependencyManagement;
import org.apache.maven.api.model.Model;
import repository.repositoryImpl.MavenRepository;
import repository.repositoryImpl.MavenRepositoryType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An artifact in a Maven repository.
 */
public class MavenComponent implements Component {
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

    public MavenComponent(String groupId, String artifactId, Version version, MavenRepository repository) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repository = repository;
    }

    @Override
    public List<MavenDependency> getDependencies() {
        //return only dependencies that are not provided or test or optional
        return this.dependencies.stream().filter(d -> !(d.getScope().equals("provided") || d.getScope().equals("test") || d.getOptional())).toList();
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
        return null;
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
        return List.of();
    }

    @Override
    public String getDescription() {
        if (this.model == null || this.model.getDescription() == null) return null;
        return this.model.getDescription();
    }

    @Override
    public String getHomepage() {
        return "";
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
    public List<String> getOniborIds() {
        return List.of();
    }

    @Override
    public List<String> getAllSwhIds() {
        return List.of();
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

    private final Lock lock = new ReentrantLock();

    @Override
    public void loadComponent() {
        if (!loaded) {
            try {
                lock.lock();
                if (!loaded) {
                    if (this.isRoot) {
                        this.loaded = true;
                        return;
                    }

                    System.out.print("Loading component: " + this.getQualifiedName() + "\t\t" + repository.getType().getName() + "... ");
                    if (this.repository != null) this.repository.loadComponent(this);
                    //if we dont have a model we try other repositories
                    if (model == null) MavenRepositoryType.tryLoadComponent(this);
                    //if we still dont have the model, we cant load the component
                    if (model == null) {
                        System.out.println("... failed");
                        return;
                    }
                    System.out.println("... success");

                    // DEPENDENCIES
                    for (var modelDependency : model.getDependencies()) {
                        // if dependency has scope "provided" or "test" or is optional, skip it
//                if (modelDependency.getScope() != null && (modelDependency.getScope().equals("provided") || modelDependency.getScope().equals("test")) || (modelDependency.getOptional() != null && Objects.equals(modelDependency.getOptional(), "true")))
//                    continue;

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

                    loaded = true;
                }
            } finally {
                lock.unlock();
            }
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

    public void printTree(String filePath) {
        try {
            PrintWriter writer;
            if (filePath == null) {
                writer = new PrintWriter(System.out);
            } else {
                writer = new PrintWriter(filePath);
            }

            this.printTree(0, "", writer);
            writer.flush();

            if (filePath != null) writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void printTree(int depth, String prependRow, PrintWriter writer) {
        if (this.isLoaded()) writer.println(this.getQualifiedName());
        else writer.println("[ERROR]: " + this.getQualifiedName() + "?");
        writer.flush();
        if (dependencies == null) return;

        for (int i = 0; i < dependencies.size(); i++) {
            MavenDependency dependency = dependencies.get(i);
            if (dependency == null) continue;
            writer.print(prependRow);
            writer.flush();

            if (i == dependencies.size() - 1) {
                writer.print("└──");
                writer.flush();
                if (dependency.getComponent() != null)
                    dependency.getComponent().printTree(depth + 1, prependRow + "   ", writer);
                else {
                    if (dependency.getOptional()) writer.print("-> [OPTIONAL]: " + dependency.getName() + "\n");
                    else if (dependency.getScope().equals("test"))
                        writer.print("-> [TEST]: " + dependency.getName() + "\n");
                    else if (dependency.getScope().equals("provided"))
                        writer.print("-> [PROVIDED]: " + dependency.getName() + "\n");
                    else writer.print("-> [ERROR]: " + dependency.getName() + "\n");
                }
                writer.flush();


            } else {
                writer.print("├──");
                writer.flush();
                if (dependency.getComponent() != null)
                    dependency.getComponent().printTree(depth + 1, prependRow + "│  ", writer);
                else {
                    if (dependency.getOptional()) writer.print("-> [OPTIONAL]: " + dependency.getName() + "\n");
                    else if (dependency.getScope().equals("test"))
                        writer.print("-> [TEST]: " + dependency.getName() + "\n");
                    else if (dependency.getScope().equals("provided"))
                        writer.print("-> [PROVIDED]: " + dependency.getName() + "\n");
                    else writer.print("-> [ERROR]: " + dependency.getName() + "\n");
                }
                writer.flush();
            }
        }
    }

    @Override
    public List<Hash> getAllHashes() {
        return hashes;
    }

    @Override
    public List<License> getAllLicences() {
        if (this.model == null || this.model.getLicenses() == null) return List.of();
        List<License> licenses = new ArrayList<>();
        for (var license : this.model.getLicenses()) {
            licenses.add(License.of(license.getName(), license.getUrl(), license.getDistribution(), license.getComments()));
        }

        return licenses;
    }

    @Override
    public List<Vulnerability> getAllVulnerabilites() {
        return Objects.requireNonNullElseGet(this.vulnerabilities, ArrayList::new);
    }

    public void setHashes(List<Hash> hashes) {
        this.hashes = hashes;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}

package data.dataImpl;


import exceptions.ArtifactBuilderException;
import service.serviceImpl.MavenCentralService;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Artifact {
    static MavenCentralService service = new MavenCentralService();
    public static final HashMap<String, Artifact> artifacts = new HashMap<>();
    public static final List<ArtifactBuilderException> errors = new ArrayList<>();
    public static boolean loadTestDependencies = false;

    String groupId;
    String artifactId;
    String version;
    Artifact parent;
    private Artifact modelParent;
    private List<Artifact> dependencies;
    private Model model;

    public static Artifact getArtifact(String groupId, String artifactId, String version) {
        return getArtifact(groupId, artifactId, version, null, true);
    }

    public static Artifact getArtifact(String groupId, String artifactId, String version, Artifact parent) {
        return getArtifact(groupId, artifactId, version, parent, true);
    }

    public static Artifact getArtifact(String groupId, String artifactId, String version, Boolean loadDependencies) {
        return getArtifact(groupId, artifactId, version, null, loadDependencies);
    }

    public static Artifact getArtifact(String groupId, String artifactId, String version, Artifact parent, Boolean loadDependencies) {
        if (groupId == null || artifactId == null || version == null) {
            throw new IllegalArgumentException("groupId, artifactId and version must not be null at " + groupId + ":" + artifactId + ":" + version + " parent:" + parent);
        }
        var key = groupId + ":" + artifactId + ":" + version;
        Artifact artifact;
        try {
            if (artifacts.containsKey(key)) {
                artifact = artifacts.get(key);
                if (!artifact.dependenciesLoaded()) {
                    artifact.loadDependencies();
                }
            } else {
                artifact = new Artifact(groupId, artifactId, version, parent, loadDependencies);
            }
        } catch (ArtifactBuilderException e) {
            errors.add(e);
            artifact = new BrokenArtifact(groupId, artifactId, version, parent);
        }
        return artifact;
    }

    private boolean dependenciesLoaded() {
        return this.dependencies != null;
    }

    Artifact() {

    }

    private Artifact(String groupId, String artifactId, String version, Artifact parent, boolean loadDependencies) throws ArtifactBuilderException {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.parent = parent;

        this.model = service.getModel(this);
        this.loadModelParent();
        Artifact.artifacts.put(this.groupId + ":" + this.artifactId + ":" + this.version, this);
        if (loadDependencies) this.loadDependencies();

    }

    private void loadModelParent() {
        if (model.getParent() != null) {
            this.modelParent = Artifact.getArtifact(model.getParent().getGroupId(), model.getParent().getArtifactId(), model.getParent().getVersion(), false);
        }
    }

    private void loadDependencies() throws ArtifactBuilderException {
        this.dependencies = new ArrayList<>();
        for (var dependency : model.getDependencies()) {

            if (dependency.getScope() != null && dependency.getScope().equals("test") && !loadTestDependencies)
                continue;
            if (dependency.getOptional() != null && dependency.getOptional().equals("true")) continue;

            var groupId = dependency.getGroupId();
            var artifactId = dependency.getArtifactId();
            var version = findVersion(dependency);

            if (dependency.getGroupId().equals("[inherited]")) {
                groupId = this.model.getParent().getGroupId();
            }

            var artifact = Artifact.getArtifact(groupId, artifactId, version, this);
            this.dependencies.add(artifact);
        }
    }

    private String findVersion(Dependency dependency) throws ArtifactBuilderException {
        String returnVersion = null;
        String version = dependency.getVersion();
        if (version == null) {
            // check if version in properties
            returnVersion = this.model.getProperties().get(dependency.getArtifactId() + ".version");
            // check if version in dependency management
            if (returnVersion == null && this.model.getDependencyManagement() != null) {
                returnVersion = this.model.getDependencyManagement().getDependencies().stream().filter(d -> d.getArtifactId().equals(dependency.getArtifactId())).findFirst().map(Dependency::getVersion).orElse(null);
                if (returnVersion != null) {
                    return this.findVersion(Dependency.newBuilder().groupId(dependency.getGroupId()).artifactId(dependency.getArtifactId()).version(returnVersion).build());
                }
            }
            //check if version in parent
            if (returnVersion == null && this.modelParent != null) {
                returnVersion = this.modelParent.findVersion(dependency);
            }
        } else if (version.startsWith("$")) {
            //get project version if version is $project.version
            if (version.equals("${project.version}")) {
                returnVersion = this.model.getVersion();
            }
            //get version in parent if version is $project.parent.version
            if (version.equals("${project.parent.version}")) {
                returnVersion = this.model.getParent().getVersion();
            }
            //find version in properties
            if (returnVersion == null)
                returnVersion = this.model.getProperties().get(version.substring(2, version.length() - 1));
            //find version in parent
            if (returnVersion == null && this.modelParent != null) {
                returnVersion = this.modelParent.findVersion(dependency);
            }
        } else if (version.equals("[inherited]")) {
            //get version of parent
            returnVersion = this.model.getParent().getVersion();
        } else if (version.startsWith("[") && version.endsWith(")")) {
            // we have a range version. we need to get the version file, and find out the highest version that matches the range
            var split = version.substring(1, version.length() - 1).split(",");
            if (split.length == 1) {
                // find the newest version
                try {
                    var versions = service.getVersions(this.groupId, this.artifactId);
                    returnVersion = versions.getHighestVersion();
                } catch (MalformedURLException e) {
                    throw new ArtifactBuilderException("Could get versioning for dependency: " + dependency);
                }
            } else {
                // find the newest version that is lower than the second number
            }
        } else {
            // non of the above cases apply, so we just try to return the version
            returnVersion = version;
        }

        if (returnVersion == null) {
            throw new ArtifactBuilderException("Could not find version for dependency: " + dependency);
        }
        return returnVersion;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public List<Artifact> getDependencies() {
        return dependencies;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "Artifact{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId + '\'' + ", version='" + version + '\'' + '}';
    }

    public void printTree(String filePath) {
        try {
            PrintWriter writer;
            if (filePath == null) {
                writer = new PrintWriter(System.out);
            } else {
                writer = new PrintWriter(filePath);
            }
            this.printTree(0, "", new HashSet<>(), writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    String treeString() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }

    private void printTree(int depth, String prependRow, Set<Artifact> visited, PrintWriter writer) {
        writer.println(treeString());
        writer.flush();
        if (dependencies == null) return;

        for (int i = 0; i < dependencies.size(); i++) {
            Artifact dependency = dependencies.get(i);
            if (dependency == null) continue;
            writer.print(prependRow);
            writer.flush();

            if (visited.contains(dependency)) {
                // Circular dependency detected
                writer.print("-CIRCULAR->" + dependency.treeString());
                return;
            }

            visited.add(dependency);
            if (i == dependencies.size() - 1) {
                writer.print("└──");
                writer.flush();
                dependency.printTree(depth + 1, prependRow + "   ", visited, writer);
            } else {
                writer.print("├──");
                writer.flush();
                dependency.printTree(depth + 1, prependRow + "│  ", visited, writer);
            }
            visited.remove(dependency);
        }
    }


}

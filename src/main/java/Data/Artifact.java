package Data;


import Services.MavenCentralService;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Artifact {
    static MavenCentralService service = new MavenCentralService();
    public static final HashMap<String, Artifact> artifacts = new HashMap<>();
    public static final List<Error> errors = new ArrayList<>();

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
        Artifact artifact = null;
        try {
            if (artifacts.containsKey(key)) {
                artifact = artifacts.get(key);
                if (!artifact.dependenciesLoaded()) {
                    artifact.loadDependencies();
                }
            } else {
                artifact = new Artifact(groupId, artifactId, version, parent, loadDependencies);
            }
        } catch (Exception e) {
            errors.add(new Error("Error loading artifact: " + groupId + ":" + artifactId + ":" + version + " " + e.getMessage()));
            artifact = new BrokenArtifact(groupId, artifactId, version, parent);
            e.printStackTrace();
        }
        return artifact;
    }

    private boolean dependenciesLoaded() {
        return this.dependencies != null;
    }

    Artifact() {

    }

    private Artifact(String groupId, String artifactId, String version, Artifact parent, boolean loadDependencies) throws MalformedURLException {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
//        this.parent = parent;
//        var t = new Thread(() -> {
//            try {
//                this.model = service.getModel(this);
//                this.loadModelParent();
//                Artifact.artifacts.put(this.groupId + ":" + this.artifactId + ":" + this.version, this);
//                if (loadDependencies) this.loadDependencies();
//            } catch (Exception e) {
//                errors.add("Error loading artifact: " + this.groupId + ":" + this.artifactId + ":" + this.version + " " + e.getMessage());
//            }
//            errors.forEach(System.err::println);
//
//        });
//        t.start();
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

    private void loadDependencies() throws MalformedURLException {
        this.dependencies = new ArrayList<>();
        for (var dependency : model.getDependencies()) {

            if (dependency.getScope() != null && dependency.getScope().equals("test")) continue;

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

    private String findVersion(Dependency dependency) throws MalformedURLException {
        String returnVersion = null;
        String version = dependency.getVersion();
        if (version == null) {
            returnVersion = this.model.getProperties().get(dependency.getArtifactId() + ".version");
            if (returnVersion == null && this.modelParent != null) {
                returnVersion = this.modelParent.findVersion(dependency);
            }
        } else if (version.startsWith("$")) {
            returnVersion = this.model.getProperties().get(version.substring(2, version.length() - 1));
            if (returnVersion == null && this.modelParent != null) {
                returnVersion = this.modelParent.findVersion(dependency);
            }
        } else if (version.equals("[inherited]")) {
            returnVersion = this.model.getParent().getVersion();
        } else if (version.startsWith("[") && version.endsWith(")")) {
            // we have a range version. we need to get the version file, and find out the highest version that matches the range
            var split = version.substring(1, version.length() - 1).split(",");
            if (split.length == 1) {
                // find the newest version
                var versions = service.getVersions(this.groupId, this.artifactId);
                returnVersion = versions.getHighestVersion();
            } else {
                // find the newest version that is lower than the second number
            }
        } else {
            returnVersion = version;
        }

        if (returnVersion == null) {
            throw new RuntimeException("Could not find version for dependency: " + dependency);
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

    @Override
    public String toString() {
        return "Artifact{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId + '\'' + ", version='" + version + '\'' + '}';
    }

    public String printTree() {
        return printTree(0, "");
    }

    String treeString() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }

    private String printTree(int depth, String prependRow) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(treeString()).append("\n");
        if (dependencies == null) return stringBuilder.toString();

        for (int i = 0; i < dependencies.size(); i++) {
            Artifact dependency = dependencies.get(i);
            if (dependency == null) continue;
            stringBuilder.append(prependRow);
            if (i == this.dependencies.size() - 1) {
                stringBuilder.append("└──");
                stringBuilder.append(dependency.printTree(depth + 1, prependRow + "   "));
            } else {
                stringBuilder.append("├──");
                stringBuilder.append(dependency.printTree(depth + 1, prependRow + "│  "));
            }
        }


        return stringBuilder.toString();
    }
}

package data.dataImpl;

public class BrokenArtifact extends Artifact {
    public BrokenArtifact(String groupId, String artifactId, String version, Artifact parent) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.parent = parent;
    }

    @Override
    String treeString() {
        return "BROKEN:" + this.groupId + ":" + this.artifactId + ":" + this.version ;
    }

    @Override
    public String toString() {
        return "BrokenArtifact{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", parent=" + parent +
                '}';
    }
}

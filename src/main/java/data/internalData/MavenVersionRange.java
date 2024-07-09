package data.internalData;

import data.Dependency;
import data.Version;
import data.VersionRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MavenVersionRange implements VersionRange {
    private List<Version> versions;
    private final MavenDependency dependency;

    public MavenVersionRange(MavenDependency dependency) {
        this.dependency = dependency;
    }

    public void addAllVersions(Collection<Version> versions) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.addAll(versions);
        Collections.sort(this.versions);
    }

    @Override
    public void addVersion(Version version) {
        if (versions == null) {
            versions = new ArrayList<>();
        }
        versions.add(version);
        Collections.sort(versions);
    }

    @Override
    public List<Version> versions() {
        return versions;
    }

    @Override
    public Version lowestVersion() {
        if (versions.isEmpty()) {
            return null;
        }
        return versions.get(0);
    }

    @Override
    public Version highestVersion() {
        if (versions.isEmpty()) {
            return null;
        }
        return versions.get(versions.size() - 1);
    }

    @Override
    public Version recommendedVersion() {
        return highestVersion();
    }

    @Override
    public Dependency dependency() {
        return dependency;
    }

    @Override
    public String version() {
        return this.recommendedVersion().version();
    }

    @Override
    public int compareTo(Version otherVersion) {
        return this.recommendedVersion().compareTo(otherVersion);
    }
}

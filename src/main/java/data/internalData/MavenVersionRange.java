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
    public List<Version> getVersions() {
        return versions;
    }

    @Override
    public Version getLowestVersion() {
        if (versions.isEmpty()) {
            return null;
        }
        return versions.get(0);
    }

    @Override
    public Version getHighestVersion() {
        if (versions.isEmpty()) {
            return null;
        }
        return versions.get(versions.size() - 1);
    }

    @Override
    public Version getRecommendedVersion() {
        return getHighestVersion();
    }

    @Override
    public Dependency getDependency() {
        return dependency;
    }

    @Override
    public String getVersion() {
        return this.getRecommendedVersion().getVersion();
    }

    @Override
    public int compareTo(Version otherVersion) {
        return this.getRecommendedVersion().compareTo(otherVersion);
    }
}

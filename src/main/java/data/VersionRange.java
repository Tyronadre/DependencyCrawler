package data;

import java.util.List;

public interface VersionRange extends Version {

    /**
     * Adds a version to this range result
     *
     * @param version the new version
     */
    void addVersion(Version version);

    /**
     * @return all versions that are in this range
     */
    List<Version> getVersions();

    /**
     * @return the lowest version in this range
     */
    Version getLowestVersion();

    /**
     * @return the highest version in this range
     */
    Version getHighestVersion();

    /**
     * @return the recommended version depending on the constraints
     */
    Version getRecommendedVersion();

    /**
     * @return the dependency to which this range belongs
     */
    Dependency getDependency();
}

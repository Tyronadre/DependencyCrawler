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
    List<Version> versions();

    /**
     * @return the lowest version in this range
     */
    Version lowestVersion();

    /**
     * @return the highest version in this range
     */
    Version highestVersion();

    /**
     * @return the recommended version depending on the constraints
     */
    Version recommendedVersion();

    /**
     * @return the dependency to which this range belongs
     */
    Dependency dependency();

}

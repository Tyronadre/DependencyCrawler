package data;

/**
 * Represents a version range request for a dependency.
 */
public interface VersionRangeRequest {
    /**
     * Returns the value of the version range.
     * @return the value of the version range
     */
    String getValue();

    /**
     * @return the dependency of the version range
     */
    Dependency getDependency();

}

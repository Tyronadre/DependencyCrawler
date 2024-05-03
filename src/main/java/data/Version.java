package data;

/**
 * Represents a version of a dependency.
 */
public interface Version extends Comparable<Version> {
    String getVersion();
}

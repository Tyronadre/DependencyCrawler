package data;

import data.internalData.VersionImpl;

/**
 * Represents a version of a dependency.
 */
public interface Version extends Comparable<Version> {
    static Version of(String version) {
        return new VersionImpl(version);
    }

    String version();
}
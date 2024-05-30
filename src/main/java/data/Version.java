package data;

import data.dataImpl.VersionImpl;
import data.dataImpl.MavenVersion;
import enums.ComponentType;

/**
 * Represents a version of a dependency.
 */
public interface Version extends Comparable<Version> {
    String getVersion();

    static Version of(ComponentType type, String string) {
        return switch (type) {
            case UNKNOWN -> new VersionImpl(string);
            case MAVEN -> new MavenVersion(string);
        };
    }
}
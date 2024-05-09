package data;

import com.google.gson.JsonObject;
import data.dataImpl.VersionImpl;
import data.dataImpl.maven.MavenVersion;
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

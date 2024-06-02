package data;

/**
 * Represents a version of a dependency.
 */
public interface Version extends Comparable<Version> {
    String getVersion();

    static Version of(String version) {
        return new Version() {
            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public int compareTo(Version o) {
                return version.compareTo(o.getVersion());
            }
        };
    }
}
package data;

public interface Version extends Comparable<Version> {

    String version();

    static Version of(String version) {
        return new VersionRecord(version);
    }

    record VersionRecord(String version) implements Version {
        @Override
        public int compareTo(Version otherVersion) {
            return this.version().compareTo(otherVersion.version());
        }
    }
}
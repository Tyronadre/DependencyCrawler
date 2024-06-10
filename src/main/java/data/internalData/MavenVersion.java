package data.internalData;

import data.Version;

import java.util.Objects;

public class MavenVersion implements Version {
    private final String version;

    public MavenVersion(String version) {
        this.version = version;
    }


    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public int compareTo(Version otherVersion) {
        return this.getVersion().compareTo(otherVersion.getVersion());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MavenVersion that)) return false;

        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version);
    }

    @Override
    public String toString() {
        return MavenVersion.class.getSimpleName() + "[" + version + "]";
    }
}
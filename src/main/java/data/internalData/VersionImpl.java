package data.internalData;

import data.Version;

import java.util.Objects;

public record VersionImpl(String version) implements Version {

    @Override
    public int compareTo(Version otherVersion) {
        return this.version().compareTo(otherVersion.version());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionImpl that)) return false;

        return Objects.equals(version, that.version);
    }

    @Override
    public String toString() {
        return VersionImpl.class.getSimpleName() + "[" + version + "]";
    }
}
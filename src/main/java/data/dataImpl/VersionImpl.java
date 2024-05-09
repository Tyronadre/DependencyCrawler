package data.dataImpl;

import data.Version;

public class VersionImpl implements Version {
    String version;

    public VersionImpl(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int compareTo(Version o) {
        throw new UnsupportedOperationException("Not supported");
    }
}

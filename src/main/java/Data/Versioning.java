package Data;

import java.util.ArrayList;
import java.util.List;

public class Versioning {
    private final List<String> versions;

    public Versioning() {
        this.versions = new ArrayList<>();
    }

    public void addVersion(String data) {
        versions.add(data);
    }

    public String getHighestVersion() {
        return versions.stream().max(String::compareTo).orElse(null);
    }
}

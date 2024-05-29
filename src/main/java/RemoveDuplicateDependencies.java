import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class RemoveDuplicateDependencies {

    static class Dependency {
        String groupId;
        String name;
        String version;

        public Dependency(String groupId, String name, String version) {
            this.groupId = groupId;
            this.name = name;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dependency that = (Dependency) o;

            if (!groupId.equals(that.groupId)) return false;
            if (!name.equals(that.name)) return false;
            return version.equals(that.version);
        }

        @Override
        public int hashCode() {
            int result = groupId.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + version.hashCode();
            return result;
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        File file = new File(RemoveDuplicateDependencies.class.getClassLoader().getResource("input_2.json").toURI());

        JsonObject json = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        JsonArray dependenciesNode = json.getAsJsonObject("application").getAsJsonArray("dependencies");

        Set<Dependency> uniqueDependencies = new HashSet<>();

        for (JsonElement node : dependenciesNode) {
            var dependencyNode = node.getAsJsonObject();
            String groupId = dependencyNode.get("groupId").getAsString();
            String name = dependencyNode.get("name").getAsString();
            String version = dependencyNode.get("version").getAsString();

            uniqueDependencies.add(new Dependency(groupId, name, version));
        }
        json.remove("dependencies");

        var newDependencies = new JsonArray();
        for (Dependency uniqueDependency : uniqueDependencies) {
            var jsonUniqueDependency = new JsonObject();
            jsonUniqueDependency.addProperty("groupId", uniqueDependency.groupId);
            jsonUniqueDependency.addProperty("name", uniqueDependency.name);
            jsonUniqueDependency.addProperty("version", uniqueDependency.version);
            newDependencies.add(jsonUniqueDependency);
        }

        json.add("dependencies", newDependencies);

        System.out.println(json);
    }
}

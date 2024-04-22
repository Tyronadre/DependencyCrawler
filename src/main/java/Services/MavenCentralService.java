package Services;

import Data.Artifact;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.HTTP;

public class CentralSonatypeOrgService {
    private final String baseUrl = "https://search.maven.org/";
    OkHttpClient client = new OkHttpClient();

    public String getArtifact(Artifact artifact) {
        System.out.println("searching for " + artifact);

        String url = baseUrl + "#artifactdetails|" + artifact.getGroupId() + "|" + artifact.getArtifactId() + "|" + artifact.getVersion() + "|";

        var request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

}

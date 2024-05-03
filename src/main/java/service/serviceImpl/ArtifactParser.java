package service.serviceImpl;

import data.dataImpl.Artifact;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;

public class ArtifactParser {

    public static Artifact fromFile(String path) {
        StringBuilder stringBuilder = new StringBuilder();

        try (FileReader reader = new FileReader(path)) {
            int character;
            while ((character = reader.read()) != -1) {
                stringBuilder.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        return Artifact.getArtifact(jsonObject.getString("groupId"), jsonObject.getString("artifactId"), jsonObject.getString("version"));
    }
}

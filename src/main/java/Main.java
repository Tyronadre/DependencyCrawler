import Data.Artifact;
import Services.ArtifactParser;
import Services.MavenCentralService;

import java.util.Objects;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, World!");

        System.out.println("Initializing artifact...");
        var artifact = ArtifactParser.fromFile(Objects.requireNonNull(Main.class.getClassLoader().getResource("input_0.json")).getPath());
        System.out.println("Artifact: " + artifact);
        System.out.println("Loaded " + Artifact.artifacts.size() + " artifacts");
        System.out.println("Error in " + Artifact.errors.size() + " artifacts");
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(artifact.printTree());

    }
}
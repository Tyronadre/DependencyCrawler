import data.dataImpl.Artifact;
import service.serviceImpl.ArtifactParser;
import service.serviceImpl.SBOMParser;
import org.apache.maven.repository.internal.MavenSessionBuilderSupplier;
import org.eclipse.aether.RepositorySystemSession;

import java.util.Objects;

import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, World!");

        System.out.println("Initializing artifact...");
        Artifact.loadTestDependencies = false;
        var artifact = ArtifactParser.fromFile(Objects.requireNonNull(Main.class.getClassLoader().getResource("input_0.json")).getPath());
        System.out.println("Artifact initialized");

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Loaded " + Artifact.artifacts.size() + " artifacts");
        System.out.println("Error in " + Artifact.errors.size() + " artifacts");

        artifact.printTree(null);

        SBOMParser sbomParser = new SBOMParser();
        sbomParser.createSBOM("sbom.json", artifact);


    }

    public void testMavenImpl(Artifact artifact) {
//        var updatePolicyAnalyzer = new DefaultUpdatePolicyAnalyzer();
//        var checksumPolicyProvider = new DefaultChecksumPolicyProvider();
//        var remoteRepositoryManager = new DefaultRemoteRepositoryManager(updatePolicyAnalyzer,checksumPolicyProvider);
//
//        var repositoryEventDispatcher = new DefaultRepositoryEventDispatcher();
//        var metadataResolver = new DefaultMetadataResolver();
//        var versionResolver = new DefaultVersionResolver();
//
//        var artifactDescriptorReader = new DefaultArtifactDescriptorReader(remoteRepositoryManager, );

//        DependencyCollector bfDependencyCollector = new BfDependencyCollector();
//        RepositorySystemSession.SessionBuilder sessionBuilder = new MavenSessionBuilderSupplier().get();
    }
}
import Data.Artifact;
import Services.ArtifactParser;
import Services.SBOMParser;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenSessionBuilderSupplier;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DependencyCollector;
import org.eclipse.aether.internal.impl.DefaultChecksumPolicyProvider;
import org.eclipse.aether.internal.impl.DefaultMetadataResolver;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.internal.impl.DefaultRepositoryEventDispatcher;
import org.eclipse.aether.internal.impl.DefaultUpdatePolicyAnalyzer;
import org.eclipse.aether.internal.impl.collect.DefaultDependencyCollector;
import org.eclipse.aether.internal.impl.collect.bf.BfDependencyCollector;
import org.eclipse.aether.util.graph.visitor.TreeDependencyVisitor;

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
        RepositorySystemSession.SessionBuilder sessionBuilder = new MavenSessionBuilderSupplier().get();
    }
}
import data.dataImpl.maven.MavenComponent;
import data.dataImpl.maven.MavenVersion;
import repository.repositoryImpl.MavenRepositoryType;
import service.serviceImpl.BFDependencyCrawlerImpl;

public class Main1 {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        var mavenRepository = MavenRepositoryType.Central.getRepository();
        MavenComponent mavenComponent = new MavenComponent("org.springframework.boot", "spring-boot-starter-test", new MavenVersion("3.1.2"), mavenRepository);

        BFDependencyCrawlerImpl bfDependencyCrawler = new BFDependencyCrawlerImpl();
        bfDependencyCrawler.loadDependencies(mavenComponent);

        mavenComponent.printTree(null);

        var googleRepository = MavenRepositoryType.Google.getRepository();
        MavenComponent googleComponent = new MavenComponent("androidx.core", "core-ktx", new MavenVersion("1.13.0"), googleRepository);

        bfDependencyCrawler.loadDependencies(googleComponent);
        System.out.println("\n Google component tree:");
        googleComponent.printTree(null);


        var comp1 = new MavenComponent("org.apache.spark", "spark-core_2.12", new MavenVersion("3.2.0"), null);
        bfDependencyCrawler.loadDependencies(comp1);
        System.out.println("\n Spark component tree:");
        comp1.printTree(null);

    }
}

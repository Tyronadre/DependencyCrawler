package repository.repositoryImpl;

import data.dataImpl.maven.MavenComponent;
import enums.RepositoryType;
import logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Default Maven Repositorys and their URLs.
 */
public enum MavenRepositoryType implements RepositoryType {
    Central("https://repo1.maven.org/maven2/"),
    Google("https://maven.google.com/"),
    Atlassian("https://packages.atlassian.com/mvn/maven-atlassian-external/"),
    Hortonworks("https://repo.hortonworks.com/content/repositories/releases/"),
    JCenter("https://jcenter.bintray.com/"),
    Sonartype("https://oss.sonatype.org/content/repositories/releases/"),
    JBossEA("https://repository.jboss.org/nexus/content/repositories/ea/"),
    KtorEAP("https://maven.pkg.jetbrains.space/public/p/ktor/eap/"),
    Atlassian_Public("https://maven.atlassian.com/content/repositories/atlassian-public/"),
    WSO2_Releases("https://maven.wso2.org/nexus/content/repositories/releases/"),
    WSO2_Public("https://maven.wso2.org/nexus/content/repositories/public/"),
    Spring_Plugins("https://repo.spring.io/plugins-release/"),
    Gigaspaces("https://maven-repository.openspaces.org/"),
    Spring_Lib_M("https://repo.spring.io/libs-milestone/"),
    BeDataDriven("https://nexus.bedatadriven.com/content/groups/public/"),
    PentahoOmni("https://nexus.pentaho.org/content/groups/omni/"),
    KotlinDev("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/"),
    XWiki_Releases("https://maven.xwiki.org/releases/"),
    Mulesoft("https://repository.mulesoft.org/nexus/content/repositories/public/"),
    Spring_Lib_Release("https://repo.spring.io/libs-release/"),
    LinkedIn("https://linkedin.jfrog.io/artifactory/open-source/"),
    ROOT("ROOT"), // root of the application
    ;

    private static final Logger logger = Logger.of("Maven");
    private final String url;

    MavenRepositoryType(String url) {
        this.url = url;
    }

    static final List<String> unloadableComponents = new ArrayList<>();

    /**
     * Try to load a component from any know repository.
     * Sets the component's repository to the first one that can load the component.
     *
     * @param mavenComponent The component to load.
     */
    public static void tryLoadComponent(MavenComponent mavenComponent) {
        if (unloadableComponents.contains(mavenComponent.getQualifiedName())) {
            return;
        }
        for (var repository : MavenRepositoryType.values()) {
            logger.info(repository.getName() + "... ");
            if (new MavenRepository(repository).loadComponent(mavenComponent)) {
                mavenComponent.setRepository(repository.getRepository());
                return;
            }
        }
        unloadableComponents.add(mavenComponent.getQualifiedName());
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getName() {
        return this.name();
    }

    // ------------ REPOSITORY LOADING ------------ //

    private static final HashMap<MavenRepositoryType, MavenRepository> loadedRepositories = new HashMap<>();

    public static MavenRepository of(MavenRepositoryType repository) {
        if (!loadedRepositories.containsKey(repository)) {
            loadedRepositories.put(repository, new MavenRepository(repository));
        }
        return loadedRepositories.get(repository);
    }

    public MavenRepository getRepository() {
        return MavenRepositoryType.of(this);
    }
}

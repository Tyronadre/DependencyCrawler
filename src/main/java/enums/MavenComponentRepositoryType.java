package enums;

/**
 * Default Maven Repositorys and their URLs.
 */
public enum MavenComponentRepositoryType implements RepositoryType {
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
    Gigaspaces("https://maven-repository.openspaces.org/"),
    BeDataDriven("https://nexus.bedatadriven.com/content/groups/public/"),
    KotlinDev("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/"),
    XWiki_Releases("https://maven.xwiki.org/releases/"),
    Mulesoft("https://repository.mulesoft.org/nexus/content/repositories/public/"),
    LinkedIn("https://linkedin.jfrog.io/artifactory/open-source/"),
    Clojars("https://clojars.org/repo/"),
    ROOT("ROOT"); // root of the application

    private final String url;

    MavenComponentRepositoryType(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getName() {
        return this.name();
    }
}

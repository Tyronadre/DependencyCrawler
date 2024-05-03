package service.serviceImpl;

import data.dataImpl.Artifact;
import data.dataImpl.Versioning;
import exceptions.ArtifactBuilderException;
import org.apache.maven.api.model.Model;
import org.apache.maven.model.v4.MavenStaxReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class MavenCentralService {
    private final String baseUrl = "https://repo1.maven.org/maven2/";

    public Model getModel(Artifact artifact) throws ArtifactBuilderException {

        URL pomUrl = null;
        try {
            var groupId = artifact.getGroupId().replace(".", "/");
            var artifactId = artifact.getArtifactId();
            var version = artifact.getVersion();
            pomUrl = URI.create(baseUrl + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom").toURL();
        } catch (Exception e) {
            throw new ArtifactBuilderException(e.getMessage());
        }

        MavenStaxReader reader = new MavenStaxReader();
        Model model;
        try (InputStream inputStream = pomUrl.openStream()) {
            model = reader.read(inputStream);
        } catch (Exception e) {
            throw new ArtifactBuilderException(e.getMessage());
        }
        return model;
    }

    public Versioning getVersions(String groupId, String artifactId) throws MalformedURLException {
        var url = URI.create(baseUrl + groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml").toURL();
        Versioning versioning = null;
        var factory = XMLInputFactory.newInstance();
        try {
            XMLEventReader reader = factory.createXMLEventReader(url.openStream());
            while (reader.hasNext()) {
                var event = reader.nextEvent();
                if (event.isStartElement()) {
                    var startElement = event.asStartElement();
                    switch (startElement.getName().toString()) {
                        case "versioning":
                            versioning = new Versioning();
                            break;
                        case "version":
                            if (versioning == null) break;
                            event = reader.nextEvent();
                            if (event.isCharacters()) {
                                versioning.addVersion(event.asCharacters().getData());
                            }
                            break;


                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return versioning;
    }
}

package service.serviceImpl.maven;

import data.Version;
import data.dataImpl.Versioning;
import exceptions.ArtifactBuilderException;
import org.apache.maven.api.model.Model;
import org.apache.maven.model.v4.MavenStaxReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MavenService {

    /**
     * Returns the versions of the given URL as Strings
     * @param url The URL to get the versions from
     * @return The versions as Strings
     */
    public List<String> getVersions(URL url) {
        List<String> versions = null;

        var factory = XMLInputFactory.newInstance();
        try {
            XMLEventReader reader = factory.createXMLEventReader(url.openStream());
            while (reader.hasNext()) {
                var event = reader.nextEvent();
                if (event.isStartElement()) {
                    var startElement = event.asStartElement();
                    switch (startElement.getName().toString()) {
                        case "versioning":
                            versions = new ArrayList<>();
                            break;
                        case "version":
                            if (versions == null) break;
                            event = reader.nextEvent();
                            if (event.isCharacters()) {
                                versions.add(event.asCharacters().getData());
                            }
                            break;
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return versions;
    }

    public Model loadModel(URL url) throws ArtifactBuilderException {
        MavenStaxReader reader = new MavenStaxReader();
        Model model;
        try (InputStream inputStream = url.openStream()) {
            model = reader.read(inputStream);
        } catch (IOException | XMLStreamException e) {
            throw new ArtifactBuilderException("Could not load model from " + url);
        }
        return model;
    }
}

package service.serviceImpl.maven;

import data.Hash;
import data.Vulnerability;
import data.dataImpl.HashImpl;
import data.dataImpl.maven.MavenComponent;
import exceptions.ArtifactBuilderException;
import org.apache.maven.api.model.Model;
import org.apache.maven.model.v4.MavenStaxReader;
import service.serviceImpl.NVDVulnerabilityService;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MavenService {
    NVDVulnerabilityService vulnerabilityService = NVDVulnerabilityService.getInstance();

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

    public List<Hash> loadHashes(String baseUrl) {
        var hashes = new ArrayList<Hash>();
        for (var algorithm : new String[]{"md5", "sha1", "sha256", "sha512"}) {
            try {
                hashes.add(loadHash(baseUrl, algorithm));
            } catch (IOException ignored) {
            }
        }
        return hashes;
    }

    private Hash loadHash(String baseUrl, String algorithm) throws IOException {
        try (InputStream inputStream = URI.create(baseUrl + "." + algorithm).toURL().openStream()) {
            var hash = new HashImpl();
            hash.setAlgorithm(algorithm);
            hash.setValue(new String(inputStream.readAllBytes()));
            return hash;
        }
    }

    public List<Vulnerability> loadVulnerabilities(MavenComponent mavenComponent) {
        return vulnerabilityService.getVulnerabilities(mavenComponent);
    }
}

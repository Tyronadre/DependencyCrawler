package service.serviceImpl;

import data.Component;
import data.ExternalReference;
import data.Hash;
import data.readData.ReadSPDXDependency;
import dependencyCrawler.DependencyCrawlerInput;
import logger.Logger;
import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.ExternalRef;
import org.spdx.library.model.SpdxDocument;
import org.spdx.library.model.SpdxElement;
import org.spdx.library.model.SpdxPackage;
import org.spdx.storage.simple.InMemSpdxStore;
import repository.repositoryImpl.ReadComponentRepository;
import service.DocumentReader;
import util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SPDXReader implements DocumentReader<Pair<SpdxDocument, Component>> {
    private static final Logger logger = Logger.of("SPDXReader");

    @Override
    public Pair<SpdxDocument, Component> readDocument(String inputFileName) {
        logger.info("Reading document as SPDX: " + inputFileName);

        var file = new File(inputFileName);
        if (!file.exists()) {
            logger.error("File does not exist: " + inputFileName);
            return null;
        }
        try (var modelStore = new InMemSpdxStore()) {
            var jacksonStore = new MultiFormatStore(modelStore, MultiFormatStore.Format.JSON);
            var documentURI = jacksonStore.deSerialize(new FileInputStream(file), true);
            var spdxDocument = new SpdxDocument(jacksonStore, documentURI, null, true);
            var rootComponent = buildComponentsAndDependencies(spdxDocument);
            return new Pair<>(spdxDocument, rootComponent);
        } catch (IOException e) {
            logger.error("Could not read from file: " + file.getAbsolutePath() + ". ", e);
        } catch (Exception e) {
            logger.error("Could not parse from file: " + file.getAbsolutePath() + ". ", e);
        }
        return null;
    }

    private Component buildComponentsAndDependencies(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {
        var componentsToBuild = new ArrayDeque<>(spdxDocument.getDocumentDescribes());
        var dependenciesToBuild = new ArrayList<Pair<SpdxElement, SpdxElement>>();
        if (componentsToBuild.size() > 1) {
            logger.error("The document specifies more than one component as Root component: " + componentsToBuild);
            throw new IllegalStateException();
        }
        Component root = null;
        HashMap<String, Component> createdComponents = new HashMap<>();
        while (!componentsToBuild.isEmpty()) {
            var spdxElement = componentsToBuild.poll();

            if (createdComponents.containsKey(spdxElement.getId())) continue;

            if (spdxElement instanceof SpdxPackage spdxPackage) {
                var newComponent = buildComponent(spdxPackage);
                if (root == null) {
                    root = newComponent;
                    newComponent.setRoot();
                }
                createdComponents.put(spdxElement.getId(), newComponent);
                for (var relation : spdxElement.getRelationships()) {
                    if (relation.getRelationshipType().toString().equals("DEPENDS_ON")) {
                        var relationElement = relation.getRelatedSpdxElement().get();
                        componentsToBuild.add(relationElement);
                        dependenciesToBuild.add(new Pair<>(spdxElement, relationElement));
                    }
                }
            }
        }
        //build dependencies
        for (var d : dependenciesToBuild) {
            var parent = createdComponents.get(d.first().getId());
            var child = createdComponents.get(d.second().getId());
            parent.addDependency(new ReadSPDXDependency(child, parent));
        }

        return root;
    }

    private Component buildComponent(SpdxPackage spdxPackage) throws InvalidSPDXAnalysisException {
        //try to figure out what type of component we have.
        List<ExternalReference> externalReferences = new ArrayList<>();
        if (!spdxPackage.getExternalRefs().isEmpty())
            externalReferences = buildAllExternalReferences(spdxPackage.getExternalRefs().stream().toList());
        var purlList = externalReferences.stream().filter(externalReference -> externalReference.getType().endsWith("purl")).toList();
        String purl = null;
        if (purlList.isEmpty()) {
            String artifactId;
            String version;
            String groupId = null;
            String possibleType = null;
            try {
                logger.info("Could not get purl for: " + spdxPackage + ". " +
                        "Trying to generate purl from other information.");
                var id = spdxPackage.getId();
                var versionSplit = id.lastIndexOf(":") == -1 ? id.lastIndexOf('-') : id.lastIndexOf(":");
                version = id.substring(versionSplit + 1);
                id = id.substring(0, versionSplit);
                var lastDot = id.lastIndexOf('.');
                if (lastDot == -1) {
                    artifactId = id.substring(7);
                } else {
                    var artifactIdSplit = id.lastIndexOf(":") == -1 ? id.substring(lastDot).indexOf('-') + lastDot : id.lastIndexOf(":");
                    artifactId = id.substring(artifactIdSplit + 1);
                    id = id.substring(0, artifactIdSplit);
                    var groupIdSplit = id.lastIndexOf('-');
                    groupId = id.substring(groupIdSplit + 1);
                    id = id.substring(7, groupIdSplit);
                    var possibleTypeSplit = id.lastIndexOf('-');
                    if (possibleTypeSplit != -1) {
                        var substring = possibleType = id.substring(0, possibleTypeSplit);
                        if (substring.contains("mvn")) {
                            possibleType = "maven";
                        }
                    }
                    if (possibleType == null) {
                        logger.info("Could not find package type. Fallback to maven");
                        possibleType = "maven";
                    }
                }

                purl = "pkg";
                purl += ":" + possibleType;
                if (groupId != null) purl += "/" + groupId;
                purl += "/" + artifactId;
                purl += "@" + version;

                logger.info("Generated " + purl + " as purl. If this purl is not correct, please specify the purl in an external reference of the component, with referenceType 'purl'");
            } catch (Exception e) {
                logger.error("Could not transform id " + spdxPackage.getId() + " to purl.", e);
            }
        } else {
            purl = purlList.getFirst().getUrl();
        }
        DependencyCrawlerInput.Type type;
        if (purl == null) {
            type = DependencyCrawlerInput.Type.OTHER;
        } else if (purl.contains("maven")) {
            type = DependencyCrawlerInput.Type.MAVEN;
        } else if (purl.contains("conan")) {
            type = DependencyCrawlerInput.Type.CONAN;
        } else if (purl.contains("android_native")) {
            type = DependencyCrawlerInput.Type.ANDROID_NATIVE;
        } else if (purl.contains("jitpack")) {
            type = DependencyCrawlerInput.Type.JITPACK;
        } else {
            type = DependencyCrawlerInput.Type.OTHER;
            logger.error("Unknown type in purl. Skipping: " + purl + ". Supported types are: pkg:maven, pkg:conan, pkg:android_native, pkg:jitpack.");
        }


        try {
            return ReadComponentRepository.getInstance().getSPDXComponent(spdxPackage, type, purl);

        } catch (Exception e) {
            logger.error("Could not get component for " + spdxPackage + ". ", e);
        }
        return null;
    }

    private List<ExternalReference> buildAllExternalReferences(List<ExternalRef> externalRefsList) {
        return externalRefsList.stream().map(this::buildExternalReference).toList();
    }

    private ExternalReference buildExternalReference(ExternalRef externalReference) {
        return new ExternalReference() {
            @Override
            public String getCategory() {
                try {
                    return externalReference.getReferenceCategory().toString();
                } catch (InvalidSPDXAnalysisException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getType() {
                try {
                    return externalReference.getReferenceType().getIndividualURI();
                } catch (InvalidSPDXAnalysisException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getUrl() {
                try {
                    return externalReference.getReferenceLocator();
                } catch (InvalidSPDXAnalysisException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getComment() {
                return null;
            }

            @Override
            public List<Hash> getHashes() {
                return null;
            }
        };
    }


}

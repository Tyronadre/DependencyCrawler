package service.serviceImpl;

import data.Component;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import exceptions.SPDXBuilderException;
import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxConstants;
import org.spdx.library.Version;
import org.spdx.library.model.ReferenceType;
import org.spdx.library.model.SpdxCreatorInformation;
import org.spdx.library.model.SpdxDocument;
import org.spdx.library.model.SpdxElement;
import org.spdx.library.model.SpdxPackage;
import org.spdx.library.model.enumerations.ChecksumAlgorithm;
import org.spdx.library.model.enumerations.Purpose;
import org.spdx.library.model.enumerations.ReferenceCategory;
import org.spdx.library.model.license.LicenseInfoFactory;
import org.spdx.storage.simple.InMemSpdxStore;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class SPDXBuilder implements DocumentBuilder {
    MultiFormatStore store;
    ModelCopyManager copyManager;
    String uri;
    SpdxDocument spdxDocument;
    Component root;

    @Override
    public void buildDocument(Component root, String outputFileName) {
        logger.info("Building SPDX document");
        this.root = root;


        var outputFileDir = outputFileName.split("/", 2);
        if (outputFileDir.length > 1) {
            //create out dir if not exists
            File outDir = new File(outputFileDir[0]);
            if (!outDir.exists()) {
                outDir.mkdir();
            }
        }

        // spdx json

        try (var out = new FileOutputStream(outputFileName + ".spdx.json")) {

            store = new MultiFormatStore(new InMemSpdxStore(), MultiFormatStore.Format.JSON_PRETTY, MultiFormatStore.Verbose.COMPACT);
            uri = "http://spdx.org/spdxdocs/spdx-example2-444504E0-4F89-41D3-9A0C-0305E82CCCCC";
            copyManager = new ModelCopyManager();
            spdxDocument = new SpdxDocument(store, uri, copyManager, true);
            buildDocument();

            // Save the SPDX document
            store.serialize(uri, out);

            logger.successLine("Done.");
        } catch (Exception e) {
            logger.errorLine("Error building SPDX document" + e.getMessage());
            e.printStackTrace();
        }


    }

    private void buildDocument() {
        try {
            spdxDocument.setCreationInfo(buildCreationInfo());
            spdxDocument.setSpecVersion(Version.CURRENT_SPDX_VERSION);
            spdxDocument.setName("SPDX-" + root.getQualifiedName());
            spdxDocument.setDataLicense(LicenseInfoFactory.parseSPDXLicenseString("CC0-1.0", store, uri, copyManager));
            spdxDocument.setComment("This SPDX document was generated by Dependency-Crawler-1.0");
            spdxDocument.getExternalDocumentRefs();
            spdxDocument.getAnnotations();
            spdxDocument.getDocumentDescribes().addAll(buildDocumentDescribes());
        } catch (InvalidSPDXAnalysisException e) {
            logger.errorLine("Error building SPDX document" + e.getMessage());
        }
    }

    private List<SpdxElement> buildDocumentDescribes() {
        var list = new ArrayList<SpdxElement>();

        for (Dependency dependency : root.getDependenciesFlat()) {
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded())
                try {
                    list.add(getSpdxElement(dependency.getComponent()));
                } catch (SPDXBuilderException e) {
                    logger.errorLine("Error building SPDX document" + e.getMessage());
                }
        }
        return list;
    }

    private SpdxCreatorInformation buildCreationInfo() {
        try {
            var creationInfo = new SpdxCreatorInformation(store, uri, UUID.randomUUID().toString(), copyManager, true);
            creationInfo.setComment("Automatically generated SPDX Document");
            creationInfo.setCreated(new SimpleDateFormat(SpdxConstants.SPDX_DATE_FORMAT).format(new Date()));
            creationInfo.getCreators().add("Tool: Dependency-Crawler-1.0");
            creationInfo.getCreators().add("Organization: Technische Universität Darmstadt");
            creationInfo.getCreators().add("Person: Henrik Bornemann");
//        creationInfo.setLicenseListVersion("3.0");
            return creationInfo;
        } catch (InvalidSPDXAnalysisException e) {
            logger.error("Error building CreationInfo" + e.getMessage());
        }
        return null;
    }

    private SpdxElement getSpdxElement(Component component) throws SPDXBuilderException {
        try {
            var spdxPackage = new SpdxPackage(store, uri, SpdxConstants.SPDX_ELEMENT_REF_PRENUM + component.getQualifiedName(), copyManager, true);
            for (Hash hash : component.getAllHashes()) {
                var checksum = spdxPackage.createChecksum(switch (hash.getAlgorithm()) {
                    case "sha1" -> ChecksumAlgorithm.SHA1;
                    case "sha256" -> ChecksumAlgorithm.SHA256;
                    case "md5" -> ChecksumAlgorithm.MD5;
                    default -> throw new SPDXBuilderException("Unexpected value: " + hash.getAlgorithm());
                }, hash.getValue());
                spdxPackage.addChecksum(checksum);
            }
//            spdxPackage.setBuiltDate();
            spdxPackage.setDescription(component.getDescription());
            var supplier = component.getSupplier();
            if (supplier != null) {
                spdxPackage.setSupplier(supplier.getName());
                spdxPackage.setOriginator(supplier.getName());
            }
            for (ExternalReference externalReference : component.getAllExternalReferences()) {
                var ref = spdxPackage.createExternalRef(ReferenceCategory.OTHER, new ReferenceType(externalReference.getType()), externalReference.getUrl(), null);
                spdxPackage.addExternalRef(ref);

            }
            spdxPackage.setPackageFileName(component.getPurl());
            spdxPackage.setDownloadLocation(component.getDownloadLocation() + ".jar");
//            spdxPackage.setFilesAnalyzed();
            if (!component.getAllLicences().isEmpty())
                spdxPackage.setLicenseDeclared(
                        LicenseInfoFactory.parseSPDXLicenseString(component.getAllLicences().get(0).getName(), store, uri, copyManager)
                );
//            spdxPackage.setPackageVerificationCode();
            spdxPackage.setPrimaryPurpose(Purpose.LIBRARY);
//            spdxPackage.setReleaseDate();
//            spdxPackage.setSourceInfo();
//            spdxPackage.setSummary();
//            spdxPackage.setValidUntilDate();
            spdxPackage.setVersionInfo(component.getVersion().getVersion());
//            spdxPackage.getFiles();
            spdxPackage.getExternalRefs();


            return spdxPackage;
        } catch (InvalidSPDXAnalysisException | SPDXBuilderException e) {
            throw new SPDXBuilderException(e.getMessage());
        }
    }
}

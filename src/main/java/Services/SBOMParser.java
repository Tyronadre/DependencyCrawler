package Services;

import Data.Artifact;
import cyclonedx.v1_6.Bom16;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SBOMParser {

    public void createSBOM(String path, Artifact artifact) {
        System.out.println("Creating SBOM");

        //build BOM
        var sbomBuilder = Bom16.Bom.newBuilder();
        sbomBuilder.setSpecVersion("1.6");
        sbomBuilder.setVersion(1);
        sbomBuilder.setSerialNumber(UUID.randomUUID().toString());


        //add dependencies
        addDependencies(sbomBuilder, artifact);

        Bom16.Bom sbom = sbomBuilder.build();

        try (FileWriter writer = new FileWriter("sbom1.json")) {
            writer.write(sbom.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (OutputStream outputStream = new FileOutputStream(path)) {
            sbom.writeTo(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDependencies(Bom16.Bom.Builder sbomBuilder, Artifact artifact) {
        if (artifact.getDependencies() != null)
            for (var dependency : artifact.getDependencies()) {
                var componentBuilder = Bom16.Component.newBuilder();
                componentBuilder.setType(Bom16.Classification.CLASSIFICATION_LIBRARY);
                componentBuilder.setBomRef(UUID.randomUUID().toString());
                if (getSupplier(dependency) != null) componentBuilder.setSupplier(getSupplier(dependency));
                if (getPublisher(dependency) != null) componentBuilder.setPublisher(getPublisher(dependency));
                if (dependency.getGroupId() != null) componentBuilder.setGroup(dependency.getGroupId());
                if (dependency.getArtifactId() != null) componentBuilder.setName(dependency.getArtifactId());
                if (dependency.getVersion() != null) componentBuilder.setVersion(dependency.getVersion());
                if (getDescription(dependency) != null) componentBuilder.setDescription(getDescription(dependency));
                if (getScope(dependency) != null) componentBuilder.setScope(getScope(dependency));
//            componentBuilder.addHashes();
//            componentBuilder.setAllLicences()
                if (getSupplier(dependency) != null) componentBuilder.setPurl(getPurl(dependency));
//            componentBuilder.setSwid();
//            componentBuilder.setPedigree();
//            componentBuilder.addExternalReferences();
//            componentBuilder.addAllComponents();
                if (getSupplier(dependency) != null) componentBuilder.addAllProperties(getProperties(dependency));
//            componentBuilder.addAllEvidence()
                if (getReleaseNotes(dependency) != null) componentBuilder.setReleaseNotes(getReleaseNotes(dependency));
//            componentBuilder.setModelCard();
//            componentBuilder.setData();
//            componentBuilder.setCryptoProperties();
                if (getManufacturer(dependency) != null) componentBuilder.setManufacturer(getManufacturer(dependency));
                if (getAuthors(dependency) != null) componentBuilder.addAllAuthors(getAuthors(artifact));
//            componentBuilder.addAllTags();
                if (getOmniborId(artifact) != null) componentBuilder.addAllOmniborId(getOmniborId(artifact));
                if (getSwhIds(dependency) != null) componentBuilder.addAllSwhid(getSwhIds(dependency));

                sbomBuilder.addComponents(componentBuilder);

                var dependencyBuilder = Bom16.Dependency.newBuilder();
                dependencyBuilder.setRef(componentBuilder.getBomRef());

                sbomBuilder.addDependencies(dependencyBuilder);
            }

    }


    private Bom16.OrganizationalEntity getSupplier(Artifact artifact) {
        var supplierBuilder = Bom16.OrganizationalEntity.newBuilder();
        var organization = artifact.getModel().getOrganization();
        if (organization != null) {
            supplierBuilder.setName(organization.getName());
            supplierBuilder.addUrl(organization.getUrl());
//            supplierBuilder.addContact(organization.getContact());
//            supplierBuilder.setAddress(organization.getAddress());
            return supplierBuilder.build();
        }
        return null;
    }

    private String getPublisher(Artifact artifact) {
        var organization = artifact.getModel().getOrganization();
        if (organization != null) return organization.getName();
        return null;
    }

    private String getDescription(Artifact artifact) {
        return artifact.getModel().getDescription();
    }

    private Bom16.Scope getScope(Artifact artifact) {
        return Bom16.Scope.SCOPE_REQUIRED;
    }

    private String getPurl(Artifact artifact) {
        return "pkg:maven/" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "@" + artifact.getVersion();
    }

    private List<Bom16.Property> getProperties(Artifact artifact) {
        var properties = artifact.getModel().getProperties();
        return properties.entrySet().stream().map(e -> Bom16.Property.newBuilder().setName(e.getKey()).setValue(e.getValue()).build()).toList();
    }

    private Bom16.ReleaseNotes getReleaseNotes(Artifact artifact) {
        return null;
    }

    private Bom16.OrganizationalEntity getManufacturer(Artifact artifact) {
        return getSupplier(artifact);
    }

    private List<Bom16.OrganizationalContact> getAuthors(Artifact artifact) {
        var authors = artifact.getModel().getContributors();
        return authors.stream().map(a -> Bom16.OrganizationalContact.newBuilder().setName(a.getName()).setEmail(a.getEmail()).setPhone(a.getOrganization()).build()).toList();
    }

    private List<String> getOmniborId(Artifact artifact) {
        return null;
    }

    private List<String> getSwhIds(Artifact artifact) {
        return null;
    }

}

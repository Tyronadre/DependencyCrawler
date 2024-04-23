package Services;

import Data.Artifact;
import cyclonedx.v1_6.Bom16;

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

        try (PrintWriter writer = new PrintWriter(path)) {
            writer.println("SBOM");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDependencies(Bom16.Bom.Builder sbomBuilder, Artifact artifact) {
        for (var dependency : artifact.getDependencies()) {
            var componentBuilder = Bom16.Component.newBuilder();
            componentBuilder.setType(Bom16.Classification.CLASSIFICATION_LIBRARY);
            componentBuilder.setBomRef(UUID.randomUUID().toString());
            componentBuilder.setSupplier(getSupplier(dependency));
            componentBuilder.setPublisher(getPublisher(dependency));
            componentBuilder.setGroup(artifact.getGroupId());
            componentBuilder.setName(artifact.getArtifactId());
            componentBuilder.setVersion(artifact.getVersion());
            componentBuilder.setDescription(getDescription(dependency));
            componentBuilder.setScope(getScope(dependency));
//            componentBuilder.addHashes();
//            componentBuilder.setAllLicences()
            componentBuilder.setPurl(getPurl(dependency));
//            componentBuilder.setSwid();
//            componentBuilder.setPedigree();
//            componentBuilder.addExternalReferences();
//            componentBuilder.addAllComponents();
            componentBuilder.addAllProperties(getProperties(dependency));
//            componentBuilder.addAllEvidence()
            componentBuilder.setReleaseNotes(getReleaseNotes(dependency));
//            componentBuilder.setModelCard();
//            componentBuilder.setData();
//            componentBuilder.setCryptoProperties();
            componentBuilder.setManufacturer(getManufacturer(dependency));
            componentBuilder.addAllAuthors(getAuthors(artifact));
//            componentBuilder.addAllTags();
            componentBuilder.addAllOmniborId(getOmniborId());
            componentBuilder.addAllSwhid(getSwhIds(dependency));

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

    private List<String> getOmniborId() {
        return null;
    }

    private List<String> getSwhIds(Artifact artifact) {
        return null;
    }

}

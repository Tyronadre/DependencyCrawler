package service.converter;

import cyclonedx.sbom.Bom16;
import data.Address;
import data.Component;
import data.ExternalReference;
import data.Hash;
import data.License;
import data.LicenseChoice;
import data.Licensing;
import data.Organization;
import data.OrganizationOrPerson;
import data.Person;
import data.Property;
import data.Timestamp;
import data.Vulnerability;
import data.VulnerabilityAffectedVersion;
import data.VulnerabilityAffects;
import data.VulnerabilityRating;
import data.VulnerabilityReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InternalMavenToBomConverter {

    public static Bom16.Metadata buildMetadata(Component root) {
        var metadataBuilder = Bom16.Metadata.newBuilder();

        Instant now = Instant.now();
        metadataBuilder.setTimestamp(com.google.protobuf.Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build());
        metadataBuilder.setComponent(buildRoot(root));
        metadataBuilder.setSupplier(buildOrganizationalEntity(Organization.of("Technische Universitaet Darmstadt", List.of("https://www.tu-darmstadt.de"), null, null)));
        metadataBuilder.setManufacturer(buildOrganizationalEntity(Organization.of("Technische Universitaet Darmstadt", List.of("https://www.tu-darmstadt.de"), null, null)));

        return metadataBuilder.build();
    }

    public static List<Bom16.Vulnerability> buildAllVulnerabilities(Collection<Vulnerability> vulnerabilities) {
        return vulnerabilities.stream().map(InternalMavenToBomConverter::buildVulnerability).toList();
    }

    public static Bom16.Vulnerability buildVulnerability(Vulnerability vulnerability) {
        var builder = Bom16.Vulnerability.newBuilder();
        Optional.ofNullable(vulnerability.getId()).ifPresent(builder::setId);
        Optional.ofNullable(vulnerability.getSource()).ifPresent(s -> builder.setSource(buildSource(s)));
        Optional.ofNullable(vulnerability.getAllReferences()).ifPresent(allRefs -> builder.addAllReferences(buildAllVulnerabilityReferences(allRefs)));
        Optional.ofNullable(vulnerability.getAllRatings()).ifPresent(allRatings -> builder.addAllRatings(buildAllVulnerabilityRatings(allRatings)));
        Optional.ofNullable(vulnerability.getAllCwes()).ifPresent(builder::addAllCwes);
        Optional.ofNullable(vulnerability.getDescription()).ifPresent(builder::setDescription);
        Optional.ofNullable(vulnerability.getDetails()).ifPresent(builder::setDetail);
        Optional.ofNullable(vulnerability.getAllRecommendations()).ifPresent(allRecs -> builder.setRecommendation(String.join("\n ", allRecs)));
        Optional.ofNullable(vulnerability.getPublished()).ifPresent(p -> builder.setPublished(buildTimestamp(p)));
        Optional.ofNullable(vulnerability.getModified()).ifPresent(m -> builder.setUpdated(buildTimestamp(m)));
        Optional.ofNullable(vulnerability.getAllProperties()).ifPresent(allProps -> builder.addAllProperties(buildAllProperties(allProps)));
        Optional.ofNullable(vulnerability.getAllAffects()).ifPresent(allAffects -> builder.addAllAffects(buildAllVulnerabilityAffects(allAffects)));
        Optional.ofNullable(vulnerability.getComponent()).ifPresent(comp -> builder.addProperties(Bom16.Property.newBuilder().setName("componentRef").setValue(comp.getPurl())));
        return builder.build();
    }

    public static List<Bom16.VulnerabilityAffects> buildAllVulnerabilityAffects(List<VulnerabilityAffects> affects) {
        return affects.stream().map(InternalMavenToBomConverter::buildVulnerabilityAffect).toList();
    }

    private static Bom16.VulnerabilityAffects buildVulnerabilityAffect(VulnerabilityAffects vulnerabilityAffects) {
        var builder = Bom16.VulnerabilityAffects.newBuilder();
        Optional.ofNullable(vulnerabilityAffects.allVersions()).ifPresent(allVersions -> builder.addAllVersions(buildAllVulnerabilityAffectedVersions(allVersions)));
        Optional.ofNullable(vulnerabilityAffects.affectedComponent()).ifPresent(comp -> builder.setRef(comp.getQualifiedName()));
        return builder.build();
    }

    private static List<Bom16.VulnerabilityAffectedVersions> buildAllVulnerabilityAffectedVersions(List<VulnerabilityAffectedVersion> allVersions) {
        return allVersions.stream().map(InternalMavenToBomConverter::buildVulnerabilityAffectedVersions).toList();
    }

    private static Bom16.VulnerabilityAffectedVersions buildVulnerabilityAffectedVersions(VulnerabilityAffectedVersion vulnerabilityAffectedVersion) {
        var builder = Bom16.VulnerabilityAffectedVersions.newBuilder();
        Optional.ofNullable(vulnerabilityAffectedVersion.version()).ifPresent(v -> builder.setVersion(v.version()));
        Optional.ofNullable(vulnerabilityAffectedVersion.versionRange()).ifPresent(builder::setRange);
        Optional.ofNullable(vulnerabilityAffectedVersion.affectedStatus()).ifPresent(s -> builder.setStatus(buildVulnerabilityAffectedStatus(s)));
        return builder.build();
    }

    private static Bom16.VulnerabilityAffectedStatus buildVulnerabilityAffectedStatus(String s) {
        return Bom16.VulnerabilityAffectedStatus.valueOf(s);
    }

    private static List<Bom16.VulnerabilityRating> buildAllVulnerabilityRatings(List<VulnerabilityRating> vulnerabilityRatings) {
        return vulnerabilityRatings.stream().map(InternalMavenToBomConverter::buildVulnerabilityRating).toList();
    }

    private static Bom16.VulnerabilityRating buildVulnerabilityRating(VulnerabilityRating vulnerabilityRating) {
        var builder = Bom16.VulnerabilityRating.newBuilder();
        Optional.ofNullable(vulnerabilityRating.source()).ifPresent(s -> builder.setSource(buildSource(s)));
        Optional.ofNullable(vulnerabilityRating.baseScore()).ifPresent(builder::setScore);
        Optional.ofNullable(vulnerabilityRating.severity()).ifPresent(s -> builder.setSeverity(buildSeverity(s)));
        Optional.ofNullable(vulnerabilityRating.method()).ifPresent(m -> builder.setMethod(buildMethod(m)));
        Optional.ofNullable(vulnerabilityRating.vector()).ifPresent(builder::setVector);
        Optional.ofNullable(vulnerabilityRating.justification()).ifPresent(builder::setJustification);
        return builder.build();
    }

    private static Bom16.ScoreMethod buildMethod(String m) {
        return switch (m) {
            case "CVSS:4.0" -> Bom16.ScoreMethod.SCORE_METHOD_CVSSV4;
            case "CVSS:3.1" -> Bom16.ScoreMethod.SCORE_METHOD_CVSSV31;
            case "CVSS:3" -> Bom16.ScoreMethod.SCORE_METHOD_CVSSV3;
            case "CVSS:2" -> Bom16.ScoreMethod.SCORE_METHOD_CVSSV2;
            default -> Bom16.ScoreMethod.valueOf(m);
        };
    }

    private static Bom16.Severity buildSeverity(String s) {
        return switch (s) {
            case "LOW" -> Bom16.Severity.SEVERITY_LOW;
            case "MODERATE" -> Bom16.Severity.SEVERITY_MEDIUM;
            case "HIGH" -> Bom16.Severity.SEVERITY_HIGH;
            case "CRITICAL" -> Bom16.Severity.SEVERITY_CRITICAL;
            default -> Bom16.Severity.valueOf(s);
        };
    }

    private static List<Bom16.VulnerabilityReference> buildAllVulnerabilityReferences(List<VulnerabilityReference> vulnerabilityReferences) {
        return vulnerabilityReferences.stream().map(InternalMavenToBomConverter::buildVulnerabilityReference).toList();
    }

    private static Bom16.VulnerabilityReference buildVulnerabilityReference(VulnerabilityReference vulnerabilityReference) {
        var builder = Bom16.VulnerabilityReference.newBuilder();
        builder.setId(vulnerabilityReference.identifier());
        builder.setSource(buildSource(vulnerabilityReference.source()));
        return builder.build();
    }

    public static Bom16.Source buildSource(Property source) {
        var builder = Bom16.Source.newBuilder();
        Optional.ofNullable(source.name()).ifPresent(builder::setName);
        Optional.ofNullable(source.value()).ifPresent(builder::setUrl);
        return builder.build();
    }

    /**
     * Build Dependencies and components recursively
     *
     * @param component       the root component
     * @param buildComponents the map that will contain all build components
     * @return the root dependency
     */
    public static List<Bom16.Dependency> buildAllDependenciesAndComponentsRecursively(Component component, Map<String, Bom16.Component> buildComponents) {
        var dependencies = new ArrayList<Bom16.Dependency>();

        buildAllDependenciesAndComponentsRecursivelyHelper(component, buildComponents, dependencies);

        return dependencies;
    }

    private static void buildAllDependenciesAndComponentsRecursivelyHelper(Component component, Map<String, Bom16.Component> buildComponents, ArrayList<Bom16.Dependency> dependencies) {


        //if this no component or has failed to load it, return
        if (component == null || !component.isLoaded()) {
            return;
        }

        //if we have build this component already return
        if (buildComponents.containsKey(component.getPurl())) {
            return;
        }

        //build the component
        var builder = Bom16.Dependency.newBuilder();
        builder.setRef(component.getPurl());
        var dependencyComponentBuilder = Bom16.Component.newBuilder(buildComponent(component));
        dependencyComponentBuilder.setScope(Bom16.Scope.SCOPE_REQUIRED);
        dependencyComponentBuilder.setBomRef(component.getPurl());
        buildComponents.put(component.getPurl(), dependencyComponentBuilder.build());

        if (component.getDependenciesFiltered().isEmpty()) return;

        //recurse on the dependencies
        for (var dep : component.getDependenciesFiltered()) {
            if (dep.getComponent() == null) continue;
            builder.addDependencies(Bom16.Dependency.newBuilder().setRef(dep.getComponent().getPurl()).build());
            buildAllDependenciesAndComponentsRecursivelyHelper(dep.getComponent(), buildComponents, dependencies);
        }

        dependencies.add(builder.build());
    }

    public static Bom16.Component buildRoot(Component root) {
        var builder = Bom16.Component.newBuilder(buildComponent(root));
        builder.setType(Bom16.Classification.CLASSIFICATION_APPLICATION);
        builder.setBomRef(root.getPurl());
        return builder.build();
    }

    public static Bom16.Component buildComponent(Component component) {
        var builder = Bom16.Component.newBuilder();
        builder.setType(Bom16.Classification.CLASSIFICATION_LIBRARY);
//        builder.setBomRef(); //do it when building dependencies, we may need to duplicate componentes then
        Optional.ofNullable(component.getSupplier()).ifPresent(supplier -> builder.setSupplier(buildOrganizationalEntity(supplier)));
        Optional.ofNullable(component.getPublisher()).ifPresent(builder::setPublisher);
        Optional.ofNullable(component.getGroup()).ifPresent(builder::setGroup);
        Optional.ofNullable(component.getArtifactId()).ifPresent(builder::setName);
        Optional.ofNullable(component.getVersion()).ifPresent(version -> builder.setVersion(version.version()));
        Optional.ofNullable(component.getDescription()).ifPresent(builder::setDescription);
//        builder.setScope //do it when building dependencies, we may need to duplicate componentes then
        Optional.ofNullable(component.getAllHashes()).ifPresent(hashes -> builder.addAllHashes(buildAllHashes(hashes)));
        Optional.ofNullable(component.getAllLicenses()).ifPresent(licenses -> builder.addAllLicenses(buildAllLicenseChoices(licenses)));
        Optional.ofNullable(component.getPurl()).ifPresent(builder::setPurl);
        Optional.ofNullable(component.getAllExternalReferences()).ifPresent(references -> builder.addAllExternalReferences(buildAllExternalReferences(references)));
        Optional.ofNullable(component.getAllProperties()).ifPresent(properties -> builder.addAllProperties(buildAllProperties(properties)));
        Optional.ofNullable(component.getManufacturer()).ifPresent(manufacturer -> builder.setManufacturer(buildOrganizationalEntity(manufacturer)));
        Optional.ofNullable(component.getAllAuthors()).ifPresent(author -> builder.addAllAuthors(buildAllOrganizationalContacts(author)));
        return builder.build();
    }

    private static List<Bom16.OrganizationalContact> buildAllOrganizationalContacts(List<Person> author) {
        return author.stream().map(InternalMavenToBomConverter::buildOrganizationContact).toList();
    }

    private static Iterable<Bom16.ExternalReference> buildAllExternalReferences(List<ExternalReference> references) {
        return references.stream().map(InternalMavenToBomConverter::buildExternalReference).toList();
    }

    private static Bom16.ExternalReference buildExternalReference(ExternalReference externalReference) {
        var builder = Bom16.ExternalReference.newBuilder();
        Optional.ofNullable(externalReference.type()).ifPresent(type -> builder.setType(buildExternalReferenceType(type)));
        Optional.ofNullable(externalReference.url()).ifPresent(builder::setUrl);
        Optional.ofNullable(externalReference.comment()).ifPresent(builder::setComment);
        return builder.build();
    }

    private static Bom16.ExternalReferenceType buildExternalReferenceType(String type) {
        try {
            return Bom16.ExternalReferenceType.valueOf(type);
        } catch (Exception ignored) {
            return Bom16.ExternalReferenceType.EXTERNAL_REFERENCE_TYPE_OTHER;
        }

    }

    private static Iterable<Bom16.LicenseChoice> buildAllLicenseChoices(List<LicenseChoice> licenses) {
        return licenses.stream().map(InternalMavenToBomConverter::buildLicenseChoice).toList();
    }

    private static Bom16.LicenseChoice buildLicenseChoice(LicenseChoice licenseChoice) {
        var builder = Bom16.LicenseChoice.newBuilder();
        Optional.ofNullable(licenseChoice.license()).ifPresent(license -> builder.setLicense(buildLicense(license)));
        Optional.ofNullable(licenseChoice.expression()).ifPresent(builder::setExpression);
        Optional.ofNullable(licenseChoice.acknowledgement()).ifPresent(acknowledgement -> builder.setAcknowledgement(buildLicenseAcknowledgementEnumeration(acknowledgement)));
        return builder.build();
    }

    private static Bom16.License buildLicense(License license) {
        var builder = Bom16.License.newBuilder();
        Optional.ofNullable(license.id()).ifPresent(builder::setId);
        Optional.ofNullable(license.name()).ifPresent(builder::setName);
        Optional.ofNullable(license.text()).ifPresent(text -> builder.setText(buildAttachedText(text)));
        Optional.ofNullable(license.url()).ifPresent(builder::setUrl);
        Optional.ofNullable(license.licensing()).ifPresent(licensing -> builder.setLicensing(buildLicensing(licensing)));
        Optional.ofNullable(license.properties()).ifPresent(properties -> builder.addAllProperties(buildAllProperties(properties)));
        Optional.ofNullable(license.acknowledgement()).ifPresent(acknowledgement -> builder.setAcknowledgement(buildLicenseAcknowledgementEnumeration(acknowledgement)));
        return builder.build();
    }

    private static Bom16.LicenseAcknowledgementEnumeration buildLicenseAcknowledgementEnumeration(String acknowledgement) {
        return Bom16.LicenseAcknowledgementEnumeration.valueOf(acknowledgement);
    }

    private static Iterable<Bom16.Property> buildAllProperties(List<Property> properties) {
        return properties.stream().distinct().map(InternalMavenToBomConverter::buildProperty).toList();
    }

    private static Bom16.Property buildProperty(Property property) {
        var builder = Bom16.Property.newBuilder();
        builder.setName(property.name());
        builder.setValue(property.value());
        return builder.build();
    }

    private static Bom16.Licensing buildLicensing(Licensing licensing) {
        var builder = Bom16.Licensing.newBuilder();
        Optional.ofNullable(licensing.altIds()).ifPresent(builder::addAllAltIds);
        Optional.ofNullable(licensing.licensor()).ifPresent(licensor -> builder.setLicensor(buildOrganizationEntityOrContact(licensor)));
        Optional.ofNullable(licensing.licensee()).ifPresent(licensee -> builder.setLicensee(buildOrganizationEntityOrContact(licensee)));
        Optional.ofNullable(licensing.purchaser()).ifPresent(purchaser -> builder.setPurchaser(buildOrganizationEntityOrContact(purchaser)));
        Optional.ofNullable(licensing.purchaseOrder()).ifPresent(builder::setPurchaseOrder);
        Optional.ofNullable(licensing.allLicenseTypes()).ifPresent(types -> builder.addAllLicenseTypes(buildAllLicenseTypes(types)));
        Optional.ofNullable(licensing.lastRenewal()).ifPresent(lastRenewal -> builder.setLastRenewal(buildTimestamp(lastRenewal)));
        Optional.ofNullable(licensing.expiration()).ifPresent(expiration -> builder.setExpiration(buildTimestamp(expiration)));
        return builder.build();
    }

    public static com.google.protobuf.Timestamp buildTimestamp(Timestamp timestamp) {
        var builder = com.google.protobuf.Timestamp.newBuilder();
        builder.setNanos(timestamp.nanos());
        builder.setSeconds(timestamp.seconds());
        return builder.build();
    }

    private static Iterable<Bom16.LicensingTypeEnum> buildAllLicenseTypes(List<String> types) {
        return types.stream().map(InternalMavenToBomConverter::buildLicenseType).toList();
    }

    private static Bom16.LicensingTypeEnum buildLicenseType(String s) {
        return Bom16.LicensingTypeEnum.valueOf(s);
    }

    private static Bom16.OrganizationalEntityOrContact buildOrganizationEntityOrContact(OrganizationOrPerson licensor) {
        var builder = Bom16.OrganizationalEntityOrContact.newBuilder();
        Optional.ofNullable(licensor.organization()).ifPresent(organization -> builder.setOrganization(buildOrganizationalEntity(organization)));
        Optional.ofNullable(licensor.person()).ifPresent(person -> builder.setIndividual(buildOrganizationContact(person)));
        return builder.build();
    }

    private static Bom16.AttachedText buildAttachedText(String text) {
        var builder = Bom16.AttachedText.newBuilder();
        builder.setValue(text);
        return builder.build();
    }

    private static Iterable<Bom16.Hash> buildAllHashes(List<Hash> hashes) {
        return hashes.stream().map(InternalMavenToBomConverter::buildHash).toList();
    }

    private static Bom16.Hash buildHash(Hash hash) {
        var builder = Bom16.Hash.newBuilder();
        try {
            builder.setAlg(Bom16.HashAlg.valueOf(hash.algorithm()));
        } catch (IllegalArgumentException e) {
            builder.setAlg(switch (hash.algorithm()) {
                case "md5" -> Bom16.HashAlg.HASH_ALG_MD_5;
                case "sha1" -> Bom16.HashAlg.HASH_ALG_SHA_1;
                case "sha256" -> Bom16.HashAlg.HASH_ALG_SHA_256;
                case "sha384" -> Bom16.HashAlg.HASH_ALG_SHA_384;
                case "sha512" -> Bom16.HashAlg.HASH_ALG_SHA_512;
                default -> Bom16.HashAlg.HASH_ALG_NULL;
            });
        }
        builder.setValue(hash.value());
        return builder.build();
    }

    public static Bom16.OrganizationalEntity buildOrganizationalEntity(Organization organization) {
        var builder = Bom16.OrganizationalEntity.newBuilder();
        Optional.ofNullable(organization.name()).ifPresent(builder::setName);
        Optional.ofNullable(organization.urls()).ifPresent(builder::addAllUrl);
        Optional.ofNullable(organization.contacts()).ifPresent(contacts -> builder.addAllContact(buildAllOrganizationContacts(contacts)));
        Optional.ofNullable(organization.address()).ifPresent(address -> builder.setAddress(buildPostalAddressType(address)));
        return builder.build();

    }

    public static Bom16.PostalAddressType buildPostalAddressType(Address address) {
        var builder = Bom16.PostalAddressType.newBuilder();
        Optional.ofNullable(address.country()).ifPresent(builder::setCountry);
        Optional.ofNullable(address.region()).ifPresent(builder::setRegion);
        Optional.ofNullable(address.city()).ifPresent(builder::setLocality);
        Optional.ofNullable(address.postOfficeBoxNumber()).ifPresent(builder::setPostOfficeBoxNumber);
        Optional.ofNullable(address.postalCode()).ifPresent(builder::setPostalCodeue);
        Optional.ofNullable(address.streetAddress()).ifPresent(builder::setStreetAddress);
        return builder.build();
    }

    private static Iterable<Bom16.OrganizationalContact> buildAllOrganizationContacts(List<Person> contacts) {
        return contacts.stream().map(InternalMavenToBomConverter::buildOrganizationContact).toList();
    }

    private static Bom16.OrganizationalContact buildOrganizationContact(Person person) {
        var builder = Bom16.OrganizationalContact.newBuilder();
        Optional.ofNullable(person.name()).ifPresent(builder::setName);
        Optional.ofNullable(person.email()).ifPresent(builder::setEmail);
        Optional.ofNullable(person.phone()).ifPresent(builder::setPhone);
        return builder.build();
    }
}

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
import data.dataImpl.OrganizationImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class InternalMavenToBomConverter {
    public static Bom16.Metadata buildMetadata(Component root) {
        var metadataBuilder = Bom16.Metadata.newBuilder();

        Instant now = Instant.now();
        metadataBuilder.setTimestamp(com.google.protobuf.Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build());
//        metadataBuilder.setTools() //TODO set this as a service in tools
        metadataBuilder.setComponent(buildRoot(root));
        metadataBuilder.setSupplier(buildOrganizationalEntity(new OrganizationImpl("Technische Universitaet Darmstadt", "https://www.tu-darmstadt.de", null)));
        metadataBuilder.setManufacturer(buildOrganizationalEntity(new OrganizationImpl("Technische Universitaet Darmstadt", "https://www.tu-darmstadt.de", null)));

        return metadataBuilder.build();
    }

    public static Bom16.Component buildRoot(Component root) {
        var builder = Bom16.Component.newBuilder(buildComponent(root));
        builder.setType(Bom16.Classification.CLASSIFICATION_APPLICATION);
        builder.setBomRef(root.getQualifiedName());
        return builder.build();
    }

    public static Bom16.Component buildComponent(Component component) {
        var builder = Bom16.Component.newBuilder();
        builder.setType(Bom16.Classification.CLASSIFICATION_LIBRARY);
//        builder.setBomRef(); //do it when building dependencies, we may need to duplicate componentes then
        Optional.ofNullable(component.getSupplier()).ifPresent(supplier -> builder.setSupplier(buildOrganizationalEntity(supplier)));
        Optional.ofNullable(component.getPublisher()).ifPresent(builder::setPublisher);
        Optional.ofNullable(component.getGroup()).ifPresent(builder::setGroup);
        Optional.ofNullable(component.getName()).ifPresent(builder::setName);
        Optional.ofNullable(component.getVersion()).ifPresent(version -> builder.setVersion(version.getVersion()));
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
        Optional.ofNullable(externalReference.getType()).ifPresent(type -> builder.setType(buildExternalReferenceType(type)));
        Optional.ofNullable(externalReference.getUrl()).ifPresent(builder::setUrl);
        Optional.ofNullable(externalReference.getComment()).ifPresent(builder::setComment);
        return builder.build();
    }

    private static Bom16.ExternalReferenceType buildExternalReferenceType(String type) {
        return Bom16.ExternalReferenceType.valueOf(type);
    }

    private static Iterable<Bom16.LicenseChoice> buildAllLicenseChoices(List<LicenseChoice> licenses) {
        return licenses.stream().map(InternalMavenToBomConverter::buildLicenseChoice).toList();
    }

    private static Bom16.LicenseChoice buildLicenseChoice(LicenseChoice licenseChoice) {
        var builder = Bom16.LicenseChoice.newBuilder();
        Optional.ofNullable(licenseChoice.getLicense()).ifPresent(license -> builder.setLicense(buildLicense(license)));
        Optional.ofNullable(licenseChoice.getExpression()).ifPresent(builder::setExpression);
        Optional.ofNullable(licenseChoice.getAcknowledgement()).ifPresent(acknowledgement -> builder.setAcknowledgement(buildLicenseAcknowledgementEnumeration(acknowledgement)));
        return builder.build();
    }

    private static Bom16.License buildLicense(License license) {
        var builder = Bom16.License.newBuilder();
        Optional.ofNullable(license.getId()).ifPresent(builder::setId);
        Optional.ofNullable(license.getName()).ifPresent(builder::setName);
        Optional.ofNullable(license.getText()).ifPresent(text -> builder.setText(buildAttachedText(text)));
        Optional.ofNullable(license.getUrl()).ifPresent(builder::setUrl);
        Optional.ofNullable(license.getLicensing()).ifPresent(licensing -> builder.setLicensing(buildLicensing(licensing)));
        Optional.ofNullable(license.getProperties()).ifPresent(properties -> builder.addAllProperties(buildAllProperties(properties)));
        Optional.ofNullable(license.getAcknowledgement()).ifPresent(acknowledgement -> builder.setAcknowledgement(buildLicenseAcknowledgementEnumeration(acknowledgement)));
        return builder.build();
    }

    private static Bom16.LicenseAcknowledgementEnumeration buildLicenseAcknowledgementEnumeration(String acknowledgement) {
        return Bom16.LicenseAcknowledgementEnumeration.valueOf(acknowledgement);
    }

    private static Iterable<Bom16.Property> buildAllProperties(List<Property> properties) {
        return properties.stream().map(InternalMavenToBomConverter::buildProperty).toList();
    }

    private static Bom16.Property buildProperty(Property property) {
        var builder = Bom16.Property.newBuilder();
        builder.setName(property.getName());
        builder.setValue(property.getValue());
        return builder.build();
    }

    private static Bom16.Licensing buildLicensing(Licensing licensing) {
        var builder = Bom16.Licensing.newBuilder();
        Optional.ofNullable(licensing.getAltIds()).ifPresent(builder::addAllAltIds);
        Optional.ofNullable(licensing.getLicensor()).ifPresent(licensor -> builder.setLicensor(buildOrganizationEntityOrContact(licensor)));
        Optional.ofNullable(licensing.getLicensee()).ifPresent(licensee -> builder.setLicensee(buildOrganizationEntityOrContact(licensee)));
        Optional.ofNullable(licensing.getPurchaser()).ifPresent(purchaser -> builder.setPurchaser(buildOrganizationEntityOrContact(purchaser)));
        Optional.ofNullable(licensing.getPurchaseOrder()).ifPresent(builder::setPurchaseOrder);
        Optional.ofNullable(licensing.getAllLicenseTypes()).ifPresent(types -> builder.addAllLicenseTypes(buildAllLicenseTypes(types)));
        Optional.ofNullable(licensing.getLastRenewal()).ifPresent(lastRenewal -> builder.setLastRenewal(buildTimestamp(lastRenewal)));
        Optional.ofNullable(licensing.getExpiration()).ifPresent(expiration -> builder.setExpiration(buildTimestamp(expiration)));
        return builder.build();
    }

    private static com.google.protobuf.Timestamp buildTimestamp(Timestamp timestamp) {
        var builder = com.google.protobuf.Timestamp.newBuilder();
        builder.setNanos(timestamp.getNanos());
        builder.setSeconds(timestamp.getSeconds());
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
        Optional.ofNullable(licensor.getOrganization()).ifPresent(organization -> builder.setOrganization(buildOrganizationalEntity(organization)));
        Optional.ofNullable(licensor.getPerson()).ifPresent(person -> builder.setIndividual(buildOrganizationContact(person)));
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
        builder.setAlg(switch (hash.getAlgorithm()) {
            case "MD5" -> Bom16.HashAlg.HASH_ALG_MD_5;
            case "SHA-1" -> Bom16.HashAlg.HASH_ALG_SHA_1;
            case "SHA-256" -> Bom16.HashAlg.HASH_ALG_SHA_256;
            case "SHA-384" -> Bom16.HashAlg.HASH_ALG_SHA_384;
            case "SHA-512" -> Bom16.HashAlg.HASH_ALG_SHA_512;
            case "SHA3-256" -> Bom16.HashAlg.HASH_ALG_SHA_3_256;
            case "SHA3-384" -> Bom16.HashAlg.HASH_ALG_SHA_3_384;
            case "SHA3-512" -> Bom16.HashAlg.HASH_ALG_SHA_3_512;
            case "BLAKE2b-256" -> Bom16.HashAlg.HASH_ALG_BLAKE_2_B_256;
            case "BLAKE2b-384" -> Bom16.HashAlg.HASH_ALG_BLAKE_2_B_384;
            case "BLAKE2b-512" -> Bom16.HashAlg.HASH_ALG_BLAKE_2_B_512;
            case "BLAKE3" -> Bom16.HashAlg.HASH_ALG_BLAKE_3;
            default -> Bom16.HashAlg.HASH_ALG_NULL;
        });
        builder.setValue(hash.getValue());
        return builder.build();
    }

    public static Bom16.OrganizationalEntity buildOrganizationalEntity(Organization organization) {
        var builder = Bom16.OrganizationalEntity.newBuilder();
        Optional.ofNullable(organization.getName()).ifPresent(builder::setName);
        Optional.ofNullable(organization.getUrls()).ifPresent(builder::addAllUrl);
        Optional.ofNullable(organization.getContacts()).ifPresent(contacts -> builder.addAllContact(buildAllOrganizationContacts(contacts)));
        Optional.ofNullable(organization.getAddress()).ifPresent(address -> builder.setAddress(buildPostalAddressType(address)));
        return builder.build();

    }

    public static Bom16.PostalAddressType buildPostalAddressType(Address address) {
        var builder = Bom16.PostalAddressType.newBuilder();
        Optional.ofNullable(address.getCountry()).ifPresent(builder::setCountry);
        Optional.ofNullable(address.getRegion()).ifPresent(builder::setRegion);
        Optional.ofNullable(address.getCity()).ifPresent(builder::setLocality);
        Optional.ofNullable(address.getPostOfficeBoxNumber()).ifPresent(builder::setPostOfficeBoxNumber);
        Optional.ofNullable(address.getPostalCode()).ifPresent(builder::setPostalCodeue);
        Optional.ofNullable(address.getStreetAddress()).ifPresent(builder::setStreetAddress);
        return builder.build();
    }

    private static Iterable<Bom16.OrganizationalContact> buildAllOrganizationContacts(List<Person> contacts) {
        return contacts.stream().map(InternalMavenToBomConverter::buildOrganizationContact).toList();
    }

    private static Bom16.OrganizationalContact buildOrganizationContact(Person person) {
        var builder = Bom16.OrganizationalContact.newBuilder();
        Optional.ofNullable(person.getName()).ifPresent(builder::setName);
        Optional.ofNullable(person.getEmail()).ifPresent(builder::setEmail);
        Optional.ofNullable(person.getPhone()).ifPresent(builder::setPhone);
        return builder.build();
    }
}

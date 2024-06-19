package service.converter;

import cyclonedx.sbom.Bom16;
import data.Address;
import data.Component;
import data.Dependency;
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
import data.Version;
import data.VulnerabilityAffectedVersion;
import data.VulnerabilityAffects;
import data.VulnerabilityRating;
import data.VulnerabilityReference;
import data.readData.ReadSBomComponent;
import data.readData.ReadSBomDependency;
import data.readData.ReadSBomVulnerability;
import repository.repositoryImpl.ReadComponentRepository;
import repository.repositoryImpl.ReadVulnerabilityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BomToInternalMavenConverter {
    private static final ReadComponentRepository componentRepository = ReadComponentRepository.getInstance();
    private static final ReadVulnerabilityRepository vulnerabilityRepository = ReadVulnerabilityRepository.getInstance();

    public static Component buildComponent(Bom16.Component bomComponent) {
        var componentFromRepository = componentRepository.getReadComponent(bomComponent);
        if (componentFromRepository != null) return componentFromRepository;

        var newComponent = ReadSBomComponent.of(bomComponent);
        componentRepository.addReadComponent(bomComponent, newComponent);
        return newComponent;
    }

    /**
     * Builds and saves the dependencies in the components.
     *
     * @param dependency the root dependency from the SBOM
     * @param parent     the parent component
     */
    public static void buildAllDependenciesRecursively(Bom16.Dependency dependency, Component parent) {
        var component = componentRepository.getReadComponent(dependency.getRef());

        if (parent != null) {
            Dependency newDependency = new ReadSBomDependency(dependency, component, parent);
            parent.addDependency(newDependency);
        }

        dependency.getDependenciesList().forEach(dep -> buildAllDependenciesRecursively(dep, component));
    }

    public static void buildAllVulnerabilities(List<Bom16.Vulnerability> bomVulnerabilities) {
        for (var bomVulnerability : bomVulnerabilities) {
            var component = componentRepository.getReadComponent(bomVulnerability.getBomRef());
            var newVul = new ReadSBomVulnerability(bomVulnerability, component);
            component.addVulnerability(newVul);

            vulnerabilityRepository.addReadVulnerability(newVul);
        }
    }

    public static List<VulnerabilityAffects> buildVulnerabilityAffects(List<Bom16.VulnerabilityAffects> bomVulnerabilityAffects) {
        var affects = new ArrayList<VulnerabilityAffects>();
        for (var bomAffect : bomVulnerabilityAffects) {

            var affectedVersions = buildVulnerabilityAffectedVersion(bomAffect);
            affects.add(new VulnerabilityAffects() {
                @Override
                public List<VulnerabilityAffectedVersion> getAllVersions() {
                    return affectedVersions;
                }

                @Override
                public Component getAffectedComponent() {
                    return componentRepository.getReadComponent(bomAffect.getRef());
                }
            });
        }
        return affects;
    }

    public static ArrayList<VulnerabilityAffectedVersion> buildVulnerabilityAffectedVersion(Bom16.VulnerabilityAffects bomAffect) {
        var affectedVersions = new ArrayList<VulnerabilityAffectedVersion>();
        for (var bomAffectedVersion : bomAffect.getVersionsList()) {
            affectedVersions.add(new VulnerabilityAffectedVersion() {
                @Override
                public Version getVersion() {
                    return bomAffectedVersion.hasVersion() ? Version.of(bomAffectedVersion.getVersion()) : null;
                }

                @Override
                public String getAffectedStatus() {
                    return bomAffectedVersion.getStatus().toString();
                }

                @Override
                public String getVersionRange() {
                    return bomAffectedVersion.getRange();
                }
            });
        }
        return affectedVersions;
    }

    public static List<VulnerabilityReference> buildVulnerabilityReferences(List<Bom16.VulnerabilityReference> bomVulnerabilityRatings) {
        var references = new ArrayList<VulnerabilityReference>();
        for (var bomReference : bomVulnerabilityRatings) {
            references.add(new VulnerabilityReference() {
                @Override
                public String getType() {
                    return null;
                }

                @Override
                public Property getSource() {
                    return bomReference.hasSource() ? buildSource(bomReference.getSource()) : null;
                }

                @Override
                public String getIdentifier() {
                    return bomReference.getId();
                }
            });
        }
        return references;
    }

    public static List<VulnerabilityRating> buildVulnerabilityRatings(List<Bom16.VulnerabilityRating> bomVulnerabilityRatings) {
        var severities = new ArrayList<VulnerabilityRating>();
        for (var bomRating : bomVulnerabilityRatings) {
            severities.add(new VulnerabilityRating() {
                @Override
                public Double getBaseScore() {
                    return bomRating.hasScore() ? bomRating.getScore() : null;
                }

                @Override
                public Double getImpactScore() {
                    return null;
                }

                @Override
                public Double getExploitabilityScore() {
                    return null;
                }

                @Override
                public String getSeverity() {
                    return bomRating.hasSeverity() ? bomRating.getSeverity().toString() : null;
                }

                @Override
                public String getMethod() {
                    return bomRating.hasMethod() ? bomRating.getMethod().toString() : null;
                }

                @Override
                public String getVector() {
                    return bomRating.getVector();
                }

                @Override
                public Property getSource() {
                    return bomRating.hasSource() ? buildSource(bomRating.getSource()) : null;
                }

                @Override
                public String getJustification() {
                    return bomRating.hasJustification() ? bomRating.getJustification() : null;
                }
            });
        }
        return severities;
    }

    public static Property buildSource(Bom16.Source source) {
        return Property.of(source.getName(), source.getUrl());
    }

    public static List<Property> buildAllProperties(List<Bom16.Property> sbomProperties) {
        return sbomProperties.stream().map(BomToInternalMavenConverter::buildProperty).toList();
    }

    public static Property buildProperty(Bom16.Property property) {
        return new Property() {
            @Override
            public String getName() {
                return property.getName();
            }

            @Override
            public String getValue() {
                return property.hasValue() ? property.getValue() : null;
            }
        };
    }

    public static List<ExternalReference> buildAllExternalReferences(List<Bom16.ExternalReference> bomExternalReferences) {
        return bomExternalReferences.stream().map(BomToInternalMavenConverter::buildExternalReference).toList();
    }

    public static ExternalReference buildExternalReference(Bom16.ExternalReference externalReference) {
        return new ExternalReference() {
            @Override
            public String getType() {
                return externalReference.getType().toString();
            }

            @Override
            public String getUrl() {
                return externalReference.getUrl();
            }

            @Override
            public String getComment() {
                return externalReference.hasComment() ? externalReference.getComment() : null;
            }

            @Override
            public List<Hash> getHashes() {
                return BomToInternalMavenConverter.buildHashes(externalReference.getHashesList());
            }
        };
    }

    public static List<LicenseChoice> buildAllLicenseChoices(List<Bom16.LicenseChoice> bomLicenses) {
        return bomLicenses.stream().map(BomToInternalMavenConverter::buildLicenseChoice).collect(Collectors.toList());
    }

    public static LicenseChoice buildLicenseChoice(Bom16.LicenseChoice licenseChoice) {
        return new LicenseChoice() {
            @Override
            public License getLicense() {
                return licenseChoice.hasLicense() ? buildLicense(licenseChoice.getLicense()) : null;
            }

            @Override
            public String getExpression() {
                return licenseChoice.hasExpression() ? licenseChoice.getExpression() : null;
            }

            @Override
            public String getAcknowledgement() {
                return licenseChoice.hasAcknowledgement() ? licenseChoice.getAcknowledgement().toString() : null;
            }
        };
    }

    public static License buildLicense(Bom16.License bomLicense) {
        return new License() {

            @Override
            public String getId() {
                return bomLicense.hasId() ? bomLicense.getId() : null;
            }

            @Override
            public String getName() {
                return bomLicense.hasName() ? bomLicense.getName() : null;
            }

            @Override
            public String getNameOrId() {
                if (getId() != null) return getId();
                return getName();
            }

            @Override
            public String getText() {
                return bomLicense.hasText() ? bomLicense.getText().getValue() : null;
            }

            @Override
            public String getUrl() {
                return bomLicense.hasUrl() ? bomLicense.getUrl() : null;
            }

            @Override
            public Licensing getLicensing() {
                return bomLicense.hasLicensing() ? buildLicensing(bomLicense.getLicensing()) : null;
            }

            @Override
            public List<Property> getProperties() {
                return buildAllProperties(bomLicense.getPropertiesList());
            }

            @Override
            public String getAcknowledgement() {
                return bomLicense.hasAcknowledgement() ? bomLicense.getAcknowledgement().toString() : null;
            }
        };
    }

    public static Licensing buildLicensing(Bom16.Licensing bomLicensing) {
        return new Licensing() {

            @Override
            public List<String> getAltIds() {
                return (bomLicensing.getAltIdsCount() > 0) ? bomLicensing.getAltIdsList() : null;
            }

            @Override
            public OrganizationOrPerson getLicensor() {
                return bomLicensing.hasLicensor() ? buildOrganizationOrContact(bomLicensing.getLicensor()) : null;
            }

            @Override
            public OrganizationOrPerson getLicensee() {
                return bomLicensing.hasLicensee() ? buildOrganizationOrContact(bomLicensing.getLicensee()) : null;
            }

            @Override
            public OrganizationOrPerson getPurchaser() {
                return bomLicensing.hasPurchaser() ? buildOrganizationOrContact(bomLicensing.getPurchaser()) : null;
            }

            @Override
            public String getPurchaseOrder() {
                return bomLicensing.hasPurchaseOrder() ? bomLicensing.getPurchaseOrder() : null;
            }

            @Override
            public List<String> getAllLicenseTypes() {
                return (bomLicensing.getLicenseTypesCount() > 0) ? bomLicensing.getLicenseTypesList().stream().map(Enum::toString).toList() : null;
            }

            @Override
            public Timestamp getLastRenewal() {
                return bomLicensing.hasLastRenewal() ? buildTimestamp(bomLicensing.getLastRenewal()) : null;
            }

            @Override
            public Timestamp getExpiration() {
                return bomLicensing.hasExpiration() ? buildTimestamp(bomLicensing.getExpiration()) : null;
            }
        };
    }

    public static Timestamp buildTimestamp(com.google.protobuf.Timestamp timestamp) {
        return new Timestamp() {
            @Override
            public int getNanos() {
                return timestamp.getNanos();
            }

            @Override
            public long getSeconds() {
                return timestamp.getSeconds();
            }
        };
    }

    public static OrganizationOrPerson buildOrganizationOrContact(Bom16.OrganizationalEntityOrContact bomOrganizationalEntityOrContact) {
        return new OrganizationOrPerson() {
            @Override
            public Organization getOrganization() {
                return (bomOrganizationalEntityOrContact.hasOrganization()) ? buildOrganization(bomOrganizationalEntityOrContact.getOrganization()) : null;
            }

            @Override
            public Person getPerson() {
                return (bomOrganizationalEntityOrContact.hasIndividual()) ? buildPerson(bomOrganizationalEntityOrContact.getIndividual(), null) : null;
            }
        };
    }

    public static Person buildPerson(Bom16.OrganizationalContact bomPerson, Organization organization) {
        return new Person() {
            @Override
            public String getName() {
                return bomPerson.hasName() ? bomPerson.getName() : null;
            }

            @Override
            public String getEmail() {
                return bomPerson.hasEmail() ? bomPerson.getEmail() : null;
            }

            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public String getPhone() {
                return bomPerson.hasPhone() ? bomPerson.getPhone() : null;
            }

            @Override
            public Organization getOrganization() {
                return organization;
            }

            @Override
            public List<String> getRoles() {
                return null;
            }
        };
    }

    public static List<Hash> buildHashes(List<Bom16.Hash> bomHashes) {
        return bomHashes.stream().map(BomToInternalMavenConverter::buildHash).toList();
    }

    public static Hash buildHash(Bom16.Hash hash) {
        return new Hash() {
            @Override
            public String getAlgorithm() {
                return hash.getAlg().toString();
            }

            @Override
            public String getValue() {
                return hash.getValue();
            }
        };
    }

    public static Organization buildOrganization(Bom16.OrganizationalEntity bomSupplier) {
        return new Organization() {
            @Override
            public String getName() {
                return bomSupplier.hasName() ? bomSupplier.getName() : null;
            }

            @Override
            public List<String> getUrls() {
                return (bomSupplier.getUrlCount() > 0) ? bomSupplier.getUrlList() : null;
            }

            @Override
            public Address getAddress() {
                return (bomSupplier.hasAddress()) ? buildAddress(bomSupplier.getAddress()) : null;
            }

            @Override
            public List<Person> getContacts() {
                return (bomSupplier.getContactCount() > 0) ? buildAllPersons(bomSupplier.getContactList(), this) : null;
            }
        };
    }

    public static Address buildAddress(Bom16.PostalAddressType bomAddress) {
        return new Address() {
            @Override
            public String getCountry() {
                return bomAddress.hasCountry() ? bomAddress.getCountry() : null;
            }

            @Override
            public String getRegion() {
                return bomAddress.hasRegion() ? bomAddress.getRegion() : null;
            }

            @Override
            public String getCity() {
                return bomAddress.hasLocality() ? bomAddress.getLocality() : null;
            }

            @Override
            public String getPostalCode() {
                return bomAddress.hasPostalCodeue() ? bomAddress.getPostalCodeue() : null;
            }

            @Override
            public String getStreetAddress() {
                return bomAddress.hasStreetAddress() ? bomAddress.getStreetAddress() : null;
            }

            @Override
            public String getPostOfficeBoxNumber() {
                return bomAddress.hasPostOfficeBoxNumber() ? bomAddress.getPostOfficeBoxNumber() : null;
            }
        };
    }

    public static List<Person> buildAllPersons(List<Bom16.OrganizationalContact> bomPersons, Organization organization) {
        return bomPersons.stream().map(person -> buildPerson(person, organization)).toList();
    }
}

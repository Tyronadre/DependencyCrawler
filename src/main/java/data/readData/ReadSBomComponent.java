package data.readData;

import cyclonedx.sbom.Bom16;
import data.Address;
import data.Component;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import data.License;
import data.LicenseChoice;
import data.Organization;
import data.Person;
import data.Property;
import data.ReadComponent;
import data.Version;
import data.Vulnerability;
import dependencyCrawler.DependencyCrawlerInput;
import logger.Logger;
import repository.ComponentRepository;
import repository.VulnerabilityRepository;
import repository.repositoryImpl.LicenseRepositoryImpl;
import repository.repositoryImpl.ReadComponentRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReadSBomComponent implements ReadComponent {

    private final Logger logger = Logger.of("ReadSBomComponent");
    Component actualComponent;
    private final Bom16.Component bomComponent;
    private final DependencyCrawlerInput.Type type;

    List<Property> properties = new ArrayList<>();
    List<Person> authors = new ArrayList<>();
    List<Dependency> dependencies = new ArrayList<>();
    List<LicenseChoice> licenseChoices = new ArrayList<>();
    List<ExternalReference> externalReferences = new ArrayList<>();
    List<Hash> hashes = new ArrayList<>();
    List<Vulnerability> vulnerabilities = new ArrayList<>();
    String groupId;
    String artifactId;
    Version version;
    String purl;
    Boolean isRoot = false;

    public ReadSBomComponent(Bom16.Component bomComponent, DependencyCrawlerInput.Type type, String purl) {
        this.bomComponent = bomComponent;
        this.type = type;
        this.purl = purl;
        if (purl != null && purl.contains("@")) {
            this.version = Version.of(purl.substring(purl.lastIndexOf('@') + 1));
            var split = purl.substring(0, purl.lastIndexOf('@')).split("/");
            this.groupId = split[split.length - 2];
            this.artifactId = split[split.length - 1];
        }

        this.actualComponent = this.getRepository().getComponent(this.groupId, this.artifactId, this.version, null);

        //Properties
        this.properties = bomComponent.getPropertiesList().stream().map(property -> Property.of(property.getName(), property.getValue())).collect(Collectors.toList());

        //Authors
        this.authors = bomComponent.getAuthorsList().stream().map(author -> Person.of(author.getName(), author.getEmail(), null, author.getPhone(), null, null)).collect(Collectors.toList());

        //LicenseChoices
        this.licenseChoices = bomComponent.getLicensesList().stream().map(
                licenseChoice -> {
                    if (licenseChoice.hasLicense()) {
                        License license;
                        if (licenseChoice.getLicense().hasId()) {
                            license = LicenseRepositoryImpl.getInstance().getLicense(licenseChoice.getLicense().getId(), licenseChoice.getLicense().getUrl(), this.getQualifiedName());
                        } else {
                            license = LicenseRepositoryImpl.getInstance().getLicense(licenseChoice.getLicense().getName(), licenseChoice.getLicense().getUrl(), this.getQualifiedName());
                        }
                        return LicenseChoice.of(List.of(license), licenseChoice.getLicense().getUrl(), licenseChoice.getAcknowledgement().toString());
                    } else if (licenseChoice.hasExpression()) {
                        return LicenseRepositoryImpl.getInstance().getLicenseChoice(licenseChoice.getExpression(), null, this.getQualifiedName());
                    }

                    logger.info("Could parse license choice in sbom for license " + licenseChoice);
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
//                licenseChoice -> LicenseChoice.of(
//                        licenseChoice.getLicense().hasId() ?
//                                LicenseRepositoryImpl.getInstance().getLicense(licenseChoice.getLicense().getId(), licenseChoice.getLicense().getUrl()) :
//                                LicenseRepositoryImpl.getInstance().getLicense(licenseChoice.getLicense().getName(), licenseChoice.getLicense().getUrl()),
//                        licenseChoice.getLicense().getUrl(),
//                        licenseChoice.getAcknowledgement().toString())).collect(Collectors.toList());

        //ExternalReferences
        this.externalReferences = bomComponent.getExternalReferencesList().stream().map(externalReference -> ExternalReference.of( externalReference.getType().toString(), externalReference.getUrl(), externalReference.getComment())).collect(Collectors.toList());

        //Hashes
        this.hashes = bomComponent.getHashesList().stream().map(hash -> Hash.of(hash.getAlg().toString(), hash.getValue())).collect(Collectors.toList());
    }


    /**
     * Updates the read component
     */
    @Override
    public synchronized void loadComponent() {
        if (this.isRoot || this.actualComponent.isLoaded())
            return;

        actualComponent.loadComponent();

        if (!actualComponent.isLoaded()) return;

        // DEPENDENCIES
        if (!this.isRoot) {
            Map<String, Dependency> dependenciesGiven = dependencies.stream().filter(it -> it.getComponent() != null).collect(Collectors.toMap(it -> it.getComponent().getGroup() + ":" + it.getComponent().getArtifactId(), Function.identity()));
            this.dependencies = this.actualComponent.getDependenciesFiltered().stream().filter(dependency -> dependency.getComponent() != null).map(dependencyLoaded -> {
                String key = dependencyLoaded.getComponent().getGroup() + ":" + dependencyLoaded.getComponent().getArtifactId();
                return dependenciesGiven.getOrDefault(key, dependencyLoaded);
            }).collect(Collectors.toList());
        }

        // LICENSES
        Map<String, LicenseChoice> licensesGiven = licenseChoices.stream().collect(Collectors.toMap(l -> l.licenses().get(0).nameOrId(), Function.identity()));
        this.licenseChoices = this.actualComponent.getAllLicenses().stream().map(licenseLoaded -> licensesGiven.getOrDefault(licenseLoaded.licenses().get(0).nameOrId(), licenseLoaded)).collect(Collectors.toList());

        // EXTERNAL REFERENCES
        this.externalReferences.addAll(actualComponent.getAllExternalReferences().stream().filter(externalReference -> externalReferences.stream().noneMatch(er -> er.url().equals(externalReference.url()))).toList());

        // HASHES
        var hashesGiven = this.hashes.stream().collect(Collectors.toMap(Hash::algorithm, Function.identity()));
        this.hashes = this.actualComponent.getAllHashes().stream().map(hashLoaded -> hashesGiven.getOrDefault(hashLoaded.algorithm(), hashLoaded)).collect(Collectors.toList());

        //Vulnerabilities
        this.vulnerabilities = VulnerabilityRepository.getInstance().getVulnerabilities(this);

    }

    @Override
    public boolean isLoaded() {
        return actualComponent.isLoaded();
    }

    @Override
    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    @Override
    public String getQualifiedName() {
        return bomComponent.getBomRef();
    }

    @Override
    public String getGroup() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Organization getSupplier() {
        if (actualComponent.getSupplier() != null)
            return actualComponent.getSupplier();

        if (!bomComponent.hasSupplier()) return null;

        var bomSupplier = bomComponent.getSupplier();
        return Organization.of(bomSupplier.hasName() ? bomSupplier.getName() : null,
                bomSupplier.getUrlCount() > 0 ? bomSupplier.getUrlList() : null,
                bomSupplier.hasAddress() ? Address.of(bomSupplier.getAddress().getCountry(), bomSupplier.getAddress().getRegion(), bomSupplier.getAddress().getLocality(), bomSupplier.getAddress().getPostalCodeue(), bomSupplier.getAddress().getStreetAddress(), bomSupplier.getAddress().getPostOfficeBoxNumber()) : null,
                bomSupplier.getContactCount() > 0 ? bomSupplier.getContactList().stream().map(contact -> Person.of(contact.getName(), contact.getEmail(), null, contact.getPhone(), null, null)).toList() : null);
    }

    @Override
    public Organization getManufacturer() {
        if (actualComponent.getManufacturer() != null)
            return actualComponent.getSupplier();

        if (!bomComponent.hasManufacturer()) return null;

        var bomManufacturer = bomComponent.getManufacturer();
        return Organization.of(bomManufacturer.hasName() ? bomManufacturer.getName() : null,
                bomManufacturer.getUrlCount() > 0 ? bomManufacturer.getUrlList() : null,
                bomManufacturer.hasAddress() ? Address.of(bomManufacturer.getAddress().getCountry(), bomManufacturer.getAddress().getRegion(), bomManufacturer.getAddress().getLocality(), bomManufacturer.getAddress().getPostalCodeue(), bomManufacturer.getAddress().getStreetAddress(), bomManufacturer.getAddress().getPostOfficeBoxNumber()) : null,
                bomManufacturer.getContactCount() > 0 ? bomManufacturer.getContactList().stream().map(contact -> Person.of(contact.getName(), contact.getEmail(), null, contact.getPhone(), null, null)).toList() : null);
    }

    @Override
    public List<Person> getContributors() {
        if (actualComponent.getContributors() != null)
            return actualComponent.getContributors();
        return authors;
    }

    @Override
    public String getDescription() {
        if (actualComponent.getDescription() != null)
            return actualComponent.getDescription();
        return (bomComponent.hasDescription()) ? bomComponent.getDescription() : null;
    }

    @Override
    public ComponentRepository getRepository() {
        return ReadComponentRepository.getInstance().getActualRepository(this);
    }

    @Override
    public String getPurl() {
        return (bomComponent.hasPurl()) ? bomComponent.getPurl() : null;
    }

    @Override
    public String getProperty(String key) {
        return properties.stream()
                .filter(property -> property.name().equals(key))
                .map(Property::value)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Component getParent() {
        return actualComponent.getParent();
    }

    @Override
    public void addDependency(Dependency dependency) {
        var checkQ = new ArrayDeque<>(List.of(dependency.getComponent()));

        while (!checkQ.isEmpty()) {
            var check = checkQ.poll();
            if (this.getQualifiedName().equals(check.getQualifiedName())) {
                logger.error("Cyclic dependency detected. Skipping dependency " + dependency.getQualifiedName() + " for component " + this.getQualifiedName());
                return;
            }
            checkQ.addAll(check.getDependenciesFiltered().stream().map(Dependency::getComponent).toList());
        }

        logger.info("Adding " + dependency.getQualifiedName() + " as dependency of " + this.getQualifiedName());

        this.dependencies.add(dependency);
    }

    @Override
    public void setRoot() {
        actualComponent.setRoot();
        this.isRoot = true;
    }

    @Override
    public List<ExternalReference> getAllExternalReferences() {
        return externalReferences;
    }

    @Override
    public List<Hash> getAllHashes() {
        return hashes;
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return this.vulnerabilities;
    }

    @Override
    public String getDownloadLocation() {
        return actualComponent.getDownloadLocation();
    }

    @Override
    public String getPublisher() {
        if (actualComponent.getPublisher() != null)
            return actualComponent.getPublisher();
        return bomComponent.getPublisher();
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return this.licenseChoices;
    }

    @Override
    public List<Property> getAllProperties() {
        return properties;
    }

    @Override
    public List<Person> getAllAuthors() {
        return this.authors;
    }

    @Override
    public void removeDependency(Dependency dependency) {
        this.dependencies.remove(dependency);
    }

    @Override
    public void setData(String key, Object value) {
        actualComponent.setData(key, value);
    }

    @Override
    public String toString() {
        return "ReadSBOMComponent{" + actualComponent.toString() + "}";
    }

    @Override
    public void removeVulnerability(Vulnerability newVul) {
        this.vulnerabilities.add(newVul);
    }

    @Override
    public void addVulnerability(Vulnerability newVul) {
        this.vulnerabilities.add(newVul);
    }

    @Override
    public Component getActualComponent() {
        return actualComponent;
    }

    @Override
    public DependencyCrawlerInput.Type getType() {
        return type;
    }

}

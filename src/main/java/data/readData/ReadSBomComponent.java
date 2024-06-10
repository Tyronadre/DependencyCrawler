package data.readData;

import cyclonedx.sbom.Bom16;
import data.*;
import enums.ComponentType;
import logger.Logger;
import org.apache.maven.api.model.Model;
import repository.ComponentRepository;
import repository.LicenseRepository;
import repository.repositoryImpl.MavenComponentRepositoryType;
import repository.repositoryImpl.ReadVulnerabilityRepository;
import service.converter.BomToInternalMavenConverter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static service.converter.BomToInternalMavenConverter.*;

public class ReadSBomComponent implements Component {
    private final Bom16.Component bomComponent;
    private final List<Dependency> dependencies;
    private final List<Property> properties;
    private Model model;
    private List<Hash> hashes;
    private List<Vulnerability> vulnerabilities;
    private final List<Person> authors;
    private final List<LicenseChoice> licenseChoices;

    private static final Logger logger = Logger.of("ReadComponent");
    private static final HashMap<String, ReadSBomComponent> components = new HashMap<>();
    private boolean isRoot = false;
    private boolean isLoaded = false;

    private ComponentRepository repository;
    private ComponentType type = ComponentType.MAVEN;

    /**
     * Returns a ReadComponent object of the given Bom16.Component object.
     * If the component is already created, it returns the existing object.
     *
     * @param bomComponent the Bom16.Component object
     * @return the ReadComponent object
     */
    public static ReadSBomComponent of(Bom16.Component bomComponent) {
        if (components.containsKey(bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion())) {
            return components.get(bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion());
        }
        ReadSBomComponent component = new ReadSBomComponent(bomComponent);
        components.put(bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion(), component);
        return component;
    }

    private ReadSBomComponent(Bom16.Component bomComponent) {
        this.bomComponent = bomComponent;
        this.dependencies = new ArrayList<>();
        this.properties = buildAllProperties(bomComponent.getPropertiesList());
        this.authors = BomToInternalMavenConverter.buildAllPersons(bomComponent.getAuthorsList(), null);
        this.vulnerabilities = new ArrayList<>();
        this.licenseChoices = buildAllLicenseChoices(this.bomComponent.getLicensesList());
    }


    /**
     * Updates the read component
     */
    @Override
    public void loadComponent() {
        if (this.isLoaded)
            return;

        if (this.isRoot) {
            this.isLoaded = true;
            return;
        }

        var start = System.currentTimeMillis();
        logger.info("Updating read component: " + this.getQualifiedName());

        if (this.repository != null) this.repository.loadComponent(this);

        switch (this.type) {
            case MAVEN -> MavenComponentRepositoryType.tryLoadComponent(this);
            default -> throw new UnsupportedOperationException("Cannot load read component of type " + type);
        }

        // LICENSES
        var licenseRepository = LicenseRepository.getInstance();
        var licenseChoices = new ArrayList<>(this.licenseChoices.stream().map(LicenseChoice::getLicense).map(License::getName).toList());
        if (this.model.getLicenses() != null) {
            var licenses = new ArrayList<>();
            for (var license : this.model.getLicenses()) {
                if (license.getName() == null) continue;
                var newLicense = licenseRepository.getLicense(license.getName(), license.getUrl());
                if (newLicense == null) {
                    logger.error("Could not resolve license for " + this.getQualifiedName() + ": " + license.getName());
                    continue;
                }

                if (licenseChoices.contains(newLicense.getName())) {
                    licenseChoices.remove(newLicense.getName());
                    continue;
                }
                logger.info("Adding new License " + newLicense + " to " + this.getQualifiedName());
                licenses.add(newLicense);
            }
        }
        if (!licenseChoices.isEmpty()) {
            logger.info("removing licenses: " + licenseChoices + " from " + this.getQualifiedName());
            licenseChoices.forEach(licenseChoice -> this.licenseChoices.removeIf(licenseChoice1 -> licenseChoice1.getLicense().getName().equals(licenseChoice)));
        }

        // VULNERABILITIES
        ReadVulnerabilityRepository.getInstance().updateReadVulnerabilities(this);

        isLoaded = true;
        logger.success("Loaded component: " + this.getQualifiedName() + " (" + (System.currentTimeMillis() - start) + "ms)");
        //throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    @Override
    public List<Dependency> getDependenciesFiltered() {
        return this.dependencies.stream()
                .filter(Dependency::shouldResolveByScope)
                .filter(Dependency::isNotOptional)
                .toList();
    }

    @Override
    public String getQualifiedName() {
        return bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion();
    }

    @Override
    public String getGroup() {
        return (bomComponent.hasGroup()) ? bomComponent.getGroup() : null;
    }

    @Override
    public String getName() {
        return bomComponent.getName();
    }

    @Override
    public Version getVersion() {
        return Version.of(bomComponent.getVersion());
    }

    @Override
    public Organization getSupplier() {
        return (bomComponent.hasSupplier()) ? buildOrganization(bomComponent.getSupplier()) : null;
    }

    @Override
    public Organization getManufacturer() {
        return (bomComponent.hasManufacturer()) ? buildOrganization(bomComponent.getManufacturer()) : null;
    }

    @Override
    public List<Person> getContributors() {
        return (bomComponent.getAuthorsCount() > 0) ? buildAllPersons(bomComponent.getAuthorsList(), null) : null;
    }

    @Override
    public String getDescription() {
        return (bomComponent.hasDescription()) ? bomComponent.getDescription() : null;
    }

    @Override
    public ComponentRepository getRepository() {
        return MavenComponentRepositoryType.of(MavenComponentRepositoryType.FILE);
    }

    @Override
    public String getPurl() {
        return (bomComponent.hasPurl()) ? bomComponent.getPurl() : null;
    }

    @Override
    public String getProperty(String key) {
        return properties.stream()
                .filter(property -> property.getName().equals(key))
                .map(Property::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Component getParent() {
        return null;
    }

    @Override
    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    @Override
    public void setRoot() {
        this.isRoot = true;
    }

    @Override
    public List<ExternalReference> getAllExternalReferences() {
        return (bomComponent.getExternalReferencesCount() > 0) ? buildAllExternalReferences(bomComponent.getExternalReferencesList()) : null;
    }

    @Override
    public List<Hash> getAllHashes() {
        return (bomComponent.getHashesCount() > 0) ? buildHashes(bomComponent.getHashesList()) : null;
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return this.vulnerabilities;
    }

    @Override
    public String getDownloadLocation() {
        if (this.repository == null)
            return null;
        return this.repository.getDownloadLocation(this);
    }

    @Override
    public List<Dependency> getDependenciesFlatFiltered() {
        return this.dependencies.stream()
                .filter(Dependency::shouldResolveByScope)
                .filter(Dependency::isNotOptional)
                .flatMap(dependency -> Stream.concat(
                        Stream.of(dependency),
                        dependency.getComponent() != null && dependency.getComponent().isLoaded()
                                ? dependency.getComponent().getDependenciesFlatFiltered().stream()
                                : Stream.empty()
                ))
                .distinct()
                .sorted(Comparator.comparing(Dependency::getQualifiedName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Component> getDependencyComponentsFlatFiltered() {
        return this.getDependenciesFlatFiltered().stream()
                .map(Dependency::getComponent)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(Component::getQualifiedName))
                .toList();
    }

    @Override
    public String getPublisher() {
        return (bomComponent.hasPublisher()) ? bomComponent.getPublisher() : null;
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
        switch (key) {
            case "model" -> this.model = (Model) value;
            case "repository" -> this.repository = (ComponentRepository) value;
            case "hashes" -> this.hashes = (List<Hash>) value;
            case "vulnerabilities" -> this.vulnerabilities = (List<Vulnerability>) value;
            default -> throw new UnsupportedOperationException("Cannot set data for key " + key);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReadSBomComponent.class.getSimpleName() + "[", "]")
                .add("bomComponent=" + bomComponent)
                .add("dependencies=" + dependencies)
                .add("properties=" + properties)
                .add("vulnerabilities=" + vulnerabilities)
                .add("authors=" + authors)
                .add("licenseChoices=" + licenseChoices)
                .add("isRoot=" + isRoot)
                .toString();
    }

    public Bom16.Component getBomComponent() {
        return bomComponent;
    }

    @Override
    public void removeVulnerability(Vulnerability newVul) {
        this.vulnerabilities.add(newVul);
    }

    @Override
    public void addVulnerability(Vulnerability newVul) {
        this.vulnerabilities.add(newVul);
    }
}

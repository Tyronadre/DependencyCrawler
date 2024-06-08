package data.dataImpl;

import cyclonedx.sbom.Bom16;
import data.*;
import enums.ComponentType;
import logger.Logger;
import org.apache.maven.api.model.Model;
import repository.ComponentRepository;
import repository.LicenseRepository;
import repository.repositoryImpl.MavenComponentRepository;
import repository.repositoryImpl.MavenRepositoryType;
import service.converter.BomToInternalMavenConverter;

import java.util.*;

import static service.converter.BomToInternalMavenConverter.*;

public class ReadComponent implements Component {
    private final Bom16.Component bomComponent;
    private final Set<Dependency> dependencies;
    private final List<Property> properties;
    private Model model;
    private List<Hash> hashes;
    private List<Vulnerability> vulnerabilities;
    private final List<Person> authors;
    private final List<LicenseChoice> licenseChoices;

    private static final Logger logger = Logger.of("ReadComponent");
    private static final HashMap<String, ReadComponent> components = new HashMap<>();
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
    public static ReadComponent of(Bom16.Component bomComponent) {
        if (components.containsKey(bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion())) {
            return components.get(bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion());
        }
        ReadComponent component = new ReadComponent(bomComponent);
        components.put(bomComponent.getGroup() + ":" + bomComponent.getName() + ":" + bomComponent.getVersion(), component);
        return component;
    }

    private ReadComponent(Bom16.Component bomComponent) {
        this.bomComponent = bomComponent;
        this.dependencies = new HashSet<>();
        this.properties = buildAllProperties(bomComponent.getPropertiesList());
        this.authors = BomToInternalMavenConverter.buildAllPersons(bomComponent.getAuthorsList(), null);
        this.vulnerabilities = new ArrayList<>();
        this.licenseChoices = buildAllLicenseChoices(this.bomComponent.getLicensesList());
    }

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
            case MAVEN -> MavenRepositoryType.tryLoadComponent(this);
            default -> throw new UnsupportedOperationException("Cannot load read component of type " + type);
        }

//        // DEPENDENCIES
//        //we keep the dependencies that we read
//        var dependenciesToRemove = new ArrayList<>(this.dependencies);
//        var dependenciesToAdd = new ArrayList<Dependency>();
//        for (var modelDependency : this.model.getDependencies()) {
//            for (var currentDependency : this.dependencies) {
//                if ((modelDependency.getGroupId() + modelDependency.getArtifactId()).equals(currentDependency.getComponent().getGroup() + currentDependency.getComponent().getName())) {
//                    dependenciesToRemove.remove(currentDependency);
//                    break;
//                }
//            }
//            dependenciesToAdd.add(new MavenDependency(modelDependency.getGroupId(), modelDependency.getArtifactId(), modelDependency.getVersion(), modelDependency.getScope(), modelDependency.getOptional(), this));
//        }
//        logger.info("Removing " + dependenciesToRemove + " dependencies from " + this.getQualifiedName());
//        dependenciesToRemove.forEach(this.dependencies::remove);
//        logger.info("Adding " + dependenciesToAdd + " dependencies to " + this.getQualifiedName());
//        this.dependencies.addAll(dependenciesToAdd);


        // PARENT
//        if (this.model.getParent() != null && this.getParent().getGroup() + this.getParent().get)
//            this.parent = this.repository.getComponent(this.model.getParent().getGroupId(), this.model.getParent().getArtifactId(), new MavenVersion(this.model.getParent().getVersion()));
//        else this.parent = null;
//
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

        isLoaded = true;
        logger.success("Loaded component: " + this.getQualifiedName() + " (" + (System.currentTimeMillis() - start) + "ms)");
        //throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public Set<Dependency> getDependencies() {
        return this.dependencies;
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
        return MavenRepositoryType.of(MavenRepositoryType.FILE);
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
    public Set<Dependency> getDependenciesFlat() {
        Set<Dependency> dependencies = new HashSet<>();
        for (var dependency : this.dependencies.stream().filter(Dependency::shouldResolveByScope).filter(Dependency::isNotOptional).toList()) {
            dependencies.add(dependency);
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded())
                dependencies.addAll(dependency.getComponent().getDependenciesFlat());
        }
        return dependencies;
    }

    @Override
    public Set<Component> getDependecyComponentsFlat() {
        Set<Component> components = new HashSet<>();
        for (var dependency : this.dependencies.stream().filter(Dependency::shouldResolveByScope).filter(Dependency::isNotOptional).toList()) {
            if (dependency.getComponent() != null && dependency.getComponent().isLoaded()) {
                components.add(dependency.getComponent());
                components.addAll(dependency.getComponent().getDependecyComponentsFlat());
            }
        }
        return components;
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
        return new StringJoiner(", ", ReadComponent.class.getSimpleName() + "[", "]")
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
}

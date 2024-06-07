package data.dataImpl;

import cyclonedx.sbom.Bom16;
import data.*;
import repository.ComponentRepository;
import repository.repositoryImpl.MavenComponentRepository;
import repository.repositoryImpl.MavenRepositoryType;
import service.converter.BomToInternalMavenConverter;

import java.util.*;

import static service.converter.BomToInternalMavenConverter.*;

public class ReadComponent implements Component {
    private final Bom16.Component bomComponent;
    private final Set<Dependency> dependencies;
    private final List<Property> properties;
    private final List<Vulnerability> vulnerabilities;
    private final List<Person> authors;
    private final List<LicenseChoice> licenseChoices;

    private static final HashMap<String, ReadComponent> components = new HashMap<>();
    private boolean isRoot = false;

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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLoaded() {
        return false;
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
    public void setRepository(MavenComponentRepository mavenRepository) {
        throw new UnsupportedOperationException();
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
    public void addVulnerability(Vulnerability vulnerability) {
        this.vulnerabilities.add(vulnerability);
    }

    @Override
    public String getDownloadLocation() {
        return null;
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

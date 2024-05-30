package data.dataImpl;

import cyclonedx.sbom.Bom16;
import data.Component;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import data.LicenseChoice;
import data.Organization;
import data.Person;
import data.Property;
import data.Version;
import data.Vulnerability;
import enums.ComponentType;
import repository.ComponentRepository;
import repository.repositoryImpl.MavenRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static service.converter.BomToInternalMavenConverter.buildAllExternalReferences;
import static service.converter.BomToInternalMavenConverter.buildAllProperties;
import static service.converter.BomToInternalMavenConverter.buildHashes;
import static service.converter.BomToInternalMavenConverter.buildOrganization;
import static service.converter.BomToInternalMavenConverter.buildAllPersons;

public class ReadComponent implements Component {
    private final Bom16.Component bomComponent;
    private final List<Dependency> dependencies;
    private final List<Property> properties;
    private final List<Vulnerability> vulnerabilities;

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
        this.dependencies = new ArrayList<>();
        this.properties = buildAllProperties(bomComponent.getPropertiesList());
        this.vulnerabilities = new ArrayList<>();
    }

    @Override
    public void loadComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLoaded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Dependency> getDependencies() {
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
        return Version.of(ComponentType.MAVEN, bomComponent.getVersion());
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
        throw new IllegalArgumentException("Not implemented");
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRepository(MavenRepository mavenRepository) {
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
    public String getPublisher() {
        return (bomComponent.hasPublisher()) ? bomComponent.getPublisher() : null;
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return List.of();
    }
}

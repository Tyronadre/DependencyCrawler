package converter.fromCycloneDX;

import converter.Converter;
import data.Dependency;
import data.ExternalReference;
import data.Hash;
import data.LicenseChoice;
import data.Organization;
import data.Person;
import data.Property;
import data.ReadComponent;
import data.Version;
import data.Vulnerability;
import dependencyCrawler.DependencyCrawlerInput;
import org.cyclonedx.model.Component;
import repository.ComponentRepository;

import java.util.List;

public class ComponentConverter implements Converter<Component, data.Component> {

    @Override
    public data.Component convert(Component component) {
        return new ReadComponent() {

            @Override
            public void loadComponent() {

            }

            @Override
            public boolean isLoaded() {
                return false;
            }

            @Override
            public List<Dependency> getDependencies() {
                return List.of();
            }

            @Override
            public String getQualifiedName() {
                return "";
            }

            @Override
            public String getGroup() {
                return "";
            }

            @Override
            public String getArtifactId() {
                return "";
            }

            @Override
            public Version getVersion() {
                return null;
            }

            @Override
            public Organization getSupplier() {
                return null;
            }

            @Override
            public Organization getManufacturer() {
                return null;
            }

            @Override
            public List<Person> getContributors() {
                return List.of();
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public ComponentRepository getRepository() {
                return null;
            }

            @Override
            public String getPurl() {
                return "";
            }

            @Override
            public String getProperty(String key) {
                return "";
            }

            @Override
            public data.Component getParent() {
                return null;
            }

            @Override
            public void addDependency(Dependency dependency) {

            }

            @Override
            public void setRoot() {

            }

            @Override
            public List<ExternalReference> getAllExternalReferences() {
                return List.of();
            }

            @Override
            public List<Hash> getAllHashes() {
                return List.of();
            }

            @Override
            public List<Vulnerability> getAllVulnerabilities() {
                return List.of();
            }

            @Override
            public String getDownloadLocation() {
                return "";
            }

            @Override
            public String getPublisher() {
                return "";
            }

            @Override
            public List<LicenseChoice> getAllLicenses() {
                return List.of();
            }

            @Override
            public List<Property> getAllProperties() {
                return List.of();
            }

            @Override
            public List<Person> getAllAuthors() {
                return List.of();
            }

            @Override
            public <T> void setData(String key, T value) {

            }

            @Override
            public void removeDependency(Dependency dependency) {

            }

            @Override
            public void removeVulnerability(Vulnerability vulnerability) {

            }

            @Override
            public void addVulnerability(Vulnerability vulnerability) {

            }

            @Override
            public data.Component getActualComponent() {
                return null;
            }

            @Override
            public DependencyCrawlerInput.Type getType() {
                return null;
            }
        };
    }
}

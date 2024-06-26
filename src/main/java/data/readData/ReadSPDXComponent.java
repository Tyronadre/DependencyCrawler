package data.readData;

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
import logger.Logger;
import org.spdx.library.model.SpdxPackage;
import repository.ComponentRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ReadSPDXComponent implements Component {
    private final static Logger logger = Logger.of("ReadSPDXComponent");
    Boolean isLoaded = false;
    SpdxPackage spdxPackage;
    ComponentRepository repository;
    List<Dependency> dependencies = new ArrayList<>();
    String groupId;
    String artifactId;
    Version version;
    String purl;

    public ReadSPDXComponent(SpdxPackage spdxPackage, ComponentRepository repository, String purl) {
        this.spdxPackage = spdxPackage;
        this.repository = repository;
        this.purl = purl;
        if (purl != null && purl.contains("@")) {
            this.version = Version.of(purl.substring(purl.lastIndexOf('@') + 1));
            var split = purl.substring(0, purl.lastIndexOf('@')).split("/");
            this.groupId = split[split.length - 2];
            this.artifactId = split[split.length - 1];
        }

    }

    @Override
    public void loadComponent() {
        if (isLoaded) return;

        if (repository.loadComponent(this) == 0){
            isLoaded = true;
        }
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public List<Dependency> getDependenciesFiltered() {
        return dependencies.stream().filter(Dependency::isNotOptional).filter(Dependency::shouldResolveByScope).toList();
    }

    @Override
    public String getQualifiedName() {
        return spdxPackage.getId();
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
        try {
            var spdxSupplier = spdxPackage.getSupplier().get();
            return Organization.of(spdxSupplier, null, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Organization getManufacturer() {
        try {
            var spdxSupplier = spdxPackage.getSupplier().get();
            return Organization.of(spdxSupplier, null, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Person> getContributors() {
        return List.of();
    }

    @Override
    public String getDescription() {
        try {
            return spdxPackage.getDescription().get();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ComponentRepository getRepository() {
        return repository;
    }

    @Override
    public String getPurl() {
        return purl;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Component getParent() {
        return null;
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

        logger.info("Adding " + dependency.getQualifiedName() + " as dependency of " + this.getQualifiedName() );

        this.dependencies.add(dependency);
    }

    @Override
    public void setRoot() {
        this.isLoaded = true;
    }

    @Override
    public List<ExternalReference> getAllExternalReferences() {
        try {
            var l = new ArrayList<ExternalReference>();
            for (var spdxRef : spdxPackage.getExternalRefs()) {
                var refTypeS = spdxRef.getReferenceType().getIndividualURI().split("/");

                l.add(ExternalReference.of(spdxRef.getReferenceCategory().toString(),  spdxRef.getReferenceLocator(), refTypeS[refTypeS.length -1], null));
            }
            return l;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<Hash> getAllHashes() {
        try {
            var l = new ArrayList<Hash>();
            for (var checksum : spdxPackage.getChecksums()) {
                l.add(Hash.of(checksum.getAlgorithm().toString(), checksum.getValue()));
            }
            return l;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<Vulnerability> getAllVulnerabilities() {
        return List.of();
    }

    @Override
    public String getDownloadLocation() {
        try {
            return spdxPackage.getDownloadLocation().get();
        } catch (Exception e) {
            return null;
        }
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
    public String toString() {
        return new StringJoiner(", ", ReadSPDXComponent.class.getSimpleName() + "[", "]")
                .add(getQualifiedName())
                .toString();
    }
}

package data.readData;

import data.Component;
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
import logger.Logger;
import org.spdx.library.model.SpdxPackage;
import repository.ComponentRepository;
import repository.LicenseRepository;
import repository.VulnerabilityRepository;
import repository.repositoryImpl.ReadComponentRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReadSPDXComponent implements ReadComponent {
    private final static Logger logger = Logger.of("ReadSPDXComponent");
    SpdxPackage spdxPackage;
    DependencyCrawlerInput.Type type;
    List<Dependency> dependencies = new ArrayList<>();
    List<LicenseChoice> licenseChoices = new ArrayList<>();
    List<ExternalReference> externalReferences = new ArrayList<>();
    List<Hash> hashes = new ArrayList<>();
    List<Vulnerability> vulnerabilities = new ArrayList<>();
    String groupId;
    String artifactId;
    Version version;
    String purl;
    Component actualComponent;
    Boolean isRoot = false;


    public ReadSPDXComponent(SpdxPackage spdxPackage, DependencyCrawlerInput.Type type, String purl) {
        this.spdxPackage = spdxPackage;
        this.type = type;
        this.purl = purl;
        if (purl != null && purl.contains("@")) {
            this.version = Version.of(purl.substring(purl.lastIndexOf('@') + 1));
            var split = purl.substring(0, purl.lastIndexOf('@')).split("/");
            this.groupId = split[split.length - 2];
            this.artifactId = split[split.length - 1];
        }

        this.actualComponent = this.getRepository().getComponent(this.groupId, this.artifactId, this.version, null);

        try {
            var declaredLicense = spdxPackage.getLicenseDeclared();
            if (declaredLicense == null || Objects.equals(declaredLicense.getId(), "NOASSERTION_LICENSE_ID"))
                throw new RuntimeException("No assertion license id");
            var license = LicenseRepository.getInstance().getLicense(declaredLicense.getId(), null);
            var licenseChoice = LicenseChoice.of(license, null, null);
            licenseChoices = List.of(licenseChoice);
        } catch (Exception ignored) {
        }

        try {
            for (var spdxRef : spdxPackage.getExternalRefs()) {
                var refTypeS = spdxRef.getReferenceType().getIndividualURI().split("/");
                externalReferences.add(ExternalReference.of(spdxRef.getReferenceCategory().toString(), spdxRef.getReferenceLocator(), refTypeS[refTypeS.length - 1], null));
            }
        } catch (Exception ignored) {
        }

        try {
            var l = new ArrayList<Hash>();
            for (var spdxHash : spdxPackage.getChecksums()) {
                l.add(Hash.of(spdxHash.getAlgorithm().toString(), spdxHash.getValue()));
            }
            this.hashes = l;
        } catch (Exception ignored) {
        }


    }

    @Override
    public synchronized void loadComponent() {
        if (actualComponent.isLoaded()) return;

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
        Map<String, LicenseChoice> licensesGiven = licenseChoices.stream().collect(Collectors.toMap(l -> l.getLicense().getNameOrId(), Function.identity()));
        this.licenseChoices = this.actualComponent.getAllLicenses().stream().map(licenseLoaded -> licensesGiven.getOrDefault(licenseLoaded.getLicense().getNameOrId(), licenseLoaded)).collect(Collectors.toList());

        // EXTERNAL REFERENCES
        this.externalReferences.addAll(actualComponent.getAllExternalReferences().stream().filter(externalReference -> externalReferences.stream().noneMatch(er -> er.getUrl().equals(externalReference.getUrl()))).toList());

        // HASHES
        var hashesGiven = this.hashes.stream().collect(Collectors.toMap(Hash::getAlgorithm, Function.identity()));
        this.hashes = this.actualComponent.getAllHashes().stream().map(hashLoaded -> hashesGiven.getOrDefault(hashLoaded.getAlgorithm(), hashLoaded)).collect(Collectors.toList());

        //Vulnerabilities
        this.vulnerabilities = VulnerabilityRepository.getInstance().getVulnerabilities(this);
    }

    @Override
    public boolean isLoaded() {
        return actualComponent.isLoaded();
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
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
        if (actualComponent.getSupplier() != null)
            return actualComponent.getSupplier();
        try {
            var spdxSupplier = spdxPackage.getSupplier().get();
            return Organization.of(spdxSupplier, null, null, null);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public Organization getManufacturer() {
        if (actualComponent.getManufacturer() != null)
            return actualComponent.getManufacturer();
        try {
            var spdxSupplier = spdxPackage.getSupplier().get();
            return Organization.of(spdxSupplier, null, null, null);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public List<Person> getContributors() {
        return actualComponent.getContributors();
    }

    @Override
    public String getDescription() {
        if (actualComponent.getDescription() != null)
            return actualComponent.getDescription();
        try {
            return spdxPackage.getDescription().get();
        } catch (Exception ignored) {
        }
        return null;

    }

    @Override
    public ComponentRepository getRepository() {
        return ReadComponentRepository.getInstance().getActualRepository(this);
    }

    @Override
    public String getPurl() {
        return purl;
    }

    @Override
    public String getProperty(String key) {
        return actualComponent.getProperty(key);
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
        return vulnerabilities;
    }

    @Override
    public String getDownloadLocation() {
        if (actualComponent.getDownloadLocation() != null)
            return actualComponent.getDownloadLocation();
        try {
            return spdxPackage.getDownloadLocation().get();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getPublisher() {
        return actualComponent.getPublisher();
    }

    @Override
    public List<LicenseChoice> getAllLicenses() {
        return licenseChoices;
    }

    @Override
    public List<Property> getAllProperties() {
        return actualComponent.getAllProperties();
    }

    @Override
    public List<Person> getAllAuthors() {
        return actualComponent.getAllAuthors();
    }

    @Override
    public <T> void setData(String key, T value) {
        actualComponent.setData(key, value);
    }

    @Override
    public void removeDependency(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeVulnerability(Vulnerability vulnerability) {
        vulnerabilities.remove(vulnerability);
    }

    @Override
    public void addVulnerability(Vulnerability vulnerability) {
        vulnerabilities.add(vulnerability);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReadSPDXComponent.class.getSimpleName() + "[", "]").add(getQualifiedName()).toString();
    }

    public DependencyCrawlerInput.Type getType() {
        return this.type;
    }

    @Override
    public Component getActualComponent() {
        return actualComponent;
    }

    @Override
    public int hashCode() {
        return this.actualComponent.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ReadSPDXComponent that)) return false;
        return this.actualComponent.equals(that.actualComponent);
    }
}

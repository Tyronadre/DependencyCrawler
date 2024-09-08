package enums;

import repository.ComponentRepository;
import repository.repositoryImpl.AndroidNativeComponentRepository;
import repository.repositoryImpl.ConanComponentRepository;
import repository.repositoryImpl.JitPackComponentRepository;
import repository.repositoryImpl.MavenComponentRepository;

public enum ComponentType {
    MAVEN(MavenComponentRepository.getInstance()),
    ANDROID_NATIVE(AndroidNativeComponentRepository.getInstance()),
    JITPACK(JitPackComponentRepository.getInstance()),
    CONAN(ConanComponentRepository.getInstance()),
    READ(null),
    SBOM(null),
    ;


    private ComponentRepository repository;

    ComponentType(ComponentRepository repository) {
        this.repository = repository;
    }

    public ComponentRepository getRepository() {
        return repository;
    }

    public void setRepository(ComponentRepository repository) {
        this.repository = repository;
    }
}

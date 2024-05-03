package service;

import data.Dependency;
import data.Version;
import exceptions.VersionResolveException;

public interface VersionResolver {

    void resolveVersion(Dependency dependency) throws VersionResolveException;

    Version getVersion(String versionString);
}

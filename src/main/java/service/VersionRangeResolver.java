package service;

import data.Dependency;
import data.VersionRange;
import exceptions.VersionRangeResolutionException;

public interface VersionRangeResolver {
    /**
     * Resolves the version range to a list of versions.
     *
     * @param dependency the dependency
     * @return the resulting versionRange
     * @throws VersionRangeResolutionException if the version range cannot be resolved
     */
    VersionRange resolveVersionRange(Dependency dependency) throws VersionRangeResolutionException;

}

package service.serviceImpl.maven;

import data.Dependency;
import data.Version;
import data.VersionRangeRequest;
import data.dataImpl.maven.MavenDependency;
import data.dataImpl.maven.MavenVersion;
import data.dataImpl.maven.MavenVersionRange;
import exceptions.VersionRangeResolutionException;
import repository.repositoryImpl.MavenRepository;
import service.VersionRangeResolver;
import service.VersionResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MavenVersionRangeResolver implements VersionRangeResolver {

    private final MavenRepository repository;

    public MavenVersionRangeResolver(MavenRepository repository) {
        this.repository = repository;
    }

    @Override
    public MavenVersionRange resolveVersionRange(Dependency dependency) throws VersionRangeResolutionException {
        String versionRangeConstraints = dependency.getVersionConstraints();
        List<VersionBound> versionBounds = new ArrayList<>();
        while (versionRangeConstraints.startsWith("[") || versionRangeConstraints.startsWith("(")) {
            int index1 = versionRangeConstraints.indexOf(')');
            int index2 = versionRangeConstraints.indexOf(']');

            int index = index2;
            if (index2 < 0 || (index1 >= 0 && index1 < index2)) {
                index = index1;
            }

            if (index < 0) {
                throw new VersionRangeResolutionException(dependency, "Unbounded version range");
            }

            versionBounds.add(new VersionBound(versionRangeConstraints.substring(0, index + 1), repository.getVersionResolver()));

            versionRangeConstraints = versionRangeConstraints.substring(index + 1).trim();
            if (versionRangeConstraints.startsWith(",")) {
                versionRangeConstraints = versionRangeConstraints.substring(1).trim();
            }
        }


        if (!versionRangeConstraints.isEmpty() || versionBounds.isEmpty()) {
            throw new VersionRangeResolutionException("Invalid version range " + dependency.getVersionConstraints() + ", expected [ or ( but got " + versionRangeConstraints);
        }

        MavenVersionRange versionRange = new MavenVersionRange((MavenDependency) dependency);

        if (versionBounds.size() == 1) {
            var bound = versionBounds.get(0);
            if (bound.lowerBound != null && bound.lowerBound.equals(bound.upperBound)) {
                versionRange.addVersion(bound.upperBound.version);
            }
        } else {
            versionRange.addAllVersions(versionBounds.stream().map(versionBound -> this.getVersions(versionBound, (MavenDependency) dependency)).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toSet()));
        }

        return versionRange;
    }

    /**
     * Get the versions that satisfy the version bound.
     *
     * @param versionBound the version bound
     * @param dependency   the dependency
     * @return the versions that satisfy the version bound
     */
    private List<MavenVersion> getVersions(VersionBound versionBound, MavenDependency dependency) {
        var versions = repository.getVersions(dependency);
        if (versionBound.lowerBound == null && versionBound.upperBound == null) {
            return versions;
        } else {
            return versions.stream().filter(version -> {
                if (versionBound.lowerBound != null) {
                    int comparison = version.compareTo(versionBound.lowerBound.version);
                    if (comparison < 0) {
                        return false;
                    } else if (comparison == 0 && !versionBound.lowerBound.inclusive) {
                        return false;
                    }
                }

                if (versionBound.upperBound != null) {
                    int comparison = version.compareTo(versionBound.upperBound.version);
                    if (comparison > 0) {
                        return false;
                    } else return comparison != 0 || versionBound.upperBound.inclusive;
                }

                return true;
            }).collect(Collectors.toList());
        }
    }


    private static class VersionBound {
        final Bound lowerBound;
        final Bound upperBound;

        private static class Bound {
            Version version;
            Boolean inclusive;

            public Bound(Version version, boolean inclusive) {
                this.version = version;
                this.inclusive = inclusive;
            }

            @Override
            public final boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Bound bound)) return false;

                return Objects.equals(version, bound.version) && Objects.equals(inclusive, bound.inclusive);
            }

            @Override
            public int hashCode() {
                int result = Objects.hashCode(version);
                result = 31 * result + Objects.hashCode(inclusive);
                return result;
            }
        }

        public VersionBound(String range, VersionResolver versionResolver) throws VersionRangeResolutionException {
            String process = requireNonNull(range, "version range cannot be null");

            boolean lowerBoundInclusive, upperBoundInclusive;
            Version lowerBound, upperBound;

            if (range.startsWith("[")) {
                lowerBoundInclusive = true;
            } else if (range.startsWith("(")) {
                lowerBoundInclusive = false;
            } else {
                throw new VersionRangeResolutionException("Invalid version range " + range + ", a range must start with either [ or (");
            }

            if (range.endsWith("]")) {
                upperBoundInclusive = true;
            } else if (range.endsWith(")")) {
                upperBoundInclusive = false;
            } else {
                throw new VersionRangeResolutionException("Invalid version range " + range + ", a range must end with either [ or (");
            }

            process = process.substring(1, process.length() - 1);

            int index = process.indexOf(",");

            if (index < 0) {
                if (!lowerBoundInclusive || !upperBoundInclusive) {
                    throw new VersionRangeResolutionException("Invalid version range " + range + ", single version must be surrounded by []");
                }

                String version = process.trim();
                if (version.endsWith(".*")) {
                    String prefix = version.substring(0, version.length() - 1);
                    lowerBound = versionResolver.getVersion(prefix);
                    upperBound = versionResolver.getVersion(prefix);
                } else {
                    lowerBound = versionResolver.getVersion(version);
                    upperBound = lowerBound;
                }
            } else {
                String parsedLowerBound = process.substring(0, index).trim();
                String parsedUpperBound = process.substring(index + 1).trim();

                if (parsedUpperBound.contains(",")) {
                    throw new VersionRangeResolutionException("Invalid version range " + range + ", bounds may not contain additional ','");
                }

                lowerBound = !parsedLowerBound.isEmpty() ? versionResolver.getVersion(parsedLowerBound) : null;
                upperBound = !parsedUpperBound.isEmpty() ? versionResolver.getVersion(parsedUpperBound) : null;

                if (upperBound != null && lowerBound != null) {
                    if (upperBound.compareTo(lowerBound) < 0) {
                        throw new VersionRangeResolutionException("Invalid version range " + range + ", lower bound must not be greater than upper bound");
                    }
                }
            }

            this.lowerBound = (lowerBound != null) ? new Bound(lowerBound, lowerBoundInclusive) : null;
            this.upperBound = (upperBound != null) ? new Bound(upperBound, upperBoundInclusive) : null;
        }
    }
}

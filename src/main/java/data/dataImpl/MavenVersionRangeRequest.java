package data.dataImpl;

import data.VersionRangeRequest;

public class MavenVersionRangeRequest implements VersionRangeRequest {

    private final String constraints;
    private final MavenDependency dependency;

    public MavenVersionRangeRequest(String constraints, MavenDependency dependency) {
        this.constraints = constraints;
        this.dependency = dependency;
    }

    @Override
    public String getValue() {
        return constraints;
    }

    @Override
    public MavenDependency getDependency() {
        return dependency;
    }
}

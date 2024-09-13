package dataNew;

import enums.ComponentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dependency {
    private String versionConstraints;
    private String version;
    private String scope;
    private Component component;
    private ComponentType type;

    public boolean shouldResolveByScope() {
        return scope == null || scope.equals("compile") || scope.equals("runtime");
    }

    public boolean isNotOptional() {
        return !scope.equals("test") && !scope.equals("provided");
    }

    public String getQualifiedName() {
        return component.getGroup() + ":" + component.getArtifactId() + ":" + versionConstraints;
    }
}

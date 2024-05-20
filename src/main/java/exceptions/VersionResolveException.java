package exceptions;

import data.Dependency;

public class VersionResolveException extends Exception {
    private final Dependency dependency;
    private final String message;

    public VersionResolveException(Dependency dependency, String message) {
        super("Version could not be resolved");
        this.dependency = dependency;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "Could not resolve version for " + dependency + ". " + message;
    }
}

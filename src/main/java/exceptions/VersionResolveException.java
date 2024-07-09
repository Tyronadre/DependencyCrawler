package exceptions;

import data.Dependency;

public class VersionResolveException extends Exception {

    public VersionResolveException(Dependency dependency, String message) {
        super("Could not resolve version for " + dependency + ". " + message);
    }

}

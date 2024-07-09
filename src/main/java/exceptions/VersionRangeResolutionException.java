package exceptions;

import data.Dependency;

public class VersionRangeResolutionException extends Exception{
    Dependency dependency;
    String message;

    public VersionRangeResolutionException(Dependency dependency, String message) {
        this.dependency = dependency;
        this.message = message;
    }

    public VersionRangeResolutionException(String message) {
        this(null, message);
    }


}

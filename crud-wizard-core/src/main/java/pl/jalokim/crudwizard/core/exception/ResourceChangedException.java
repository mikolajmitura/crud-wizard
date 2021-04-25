package pl.jalokim.crudwizard.core.exception;

public class ResourceChangedException extends RuntimeException {

    public ResourceChangedException() {
        super("Resource has been modified by another user.");
    }
}

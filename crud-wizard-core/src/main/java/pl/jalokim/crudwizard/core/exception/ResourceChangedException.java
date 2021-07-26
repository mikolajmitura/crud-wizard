package pl.jalokim.crudwizard.core.exception;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;

public class ResourceChangedException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_CHANGED_EXCEPTION_DEFAULT_MESSAGE = "ResourceChangedException.default.message";

    public ResourceChangedException() {
        super(wrapAsPlaceholder(RESOURCE_CHANGED_EXCEPTION_DEFAULT_MESSAGE));
    }
}

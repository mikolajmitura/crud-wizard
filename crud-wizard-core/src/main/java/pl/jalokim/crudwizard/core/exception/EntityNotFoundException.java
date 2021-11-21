package pl.jalokim.crudwizard.core.exception;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;

import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class EntityNotFoundException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public static final String EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY = "EntityNotFoundException.default.message";
    public static final String EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY = "EntityNotFoundException.default.concrete.message";

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Object id) {
        super(createMessagePlaceholder(EXCEPTION_DEFAULT_MESSAGE_PROPERTY_KEY, id));
    }

    /**
     * @param id id of entity
     * @param entityType will be translated to placeholder like "{full.package.SomeEntity}"
     */
    public EntityNotFoundException(Object id, Class<?> entityType) {
        this(id, wrapAsPlaceholder(entityType));
    }

    /**
     * @param id id of entity
     * @param translatedEntityNameOrPropertyKey real translated entity name or just property key provided as "{some.entity.name.property.key}"
     */
    public EntityNotFoundException(Object id, String translatedEntityNameOrPropertyKey) {
        super(createMessagePlaceholder(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY, id, translatedEntityNameOrPropertyKey));
    }

    public EntityNotFoundException(MessagePlaceholder messagePlaceHolder) {
        super(messagePlaceHolder);
    }

    public EntityNotFoundException(MessagePlaceholder messagePlaceHolder, Throwable ex) {
        super(messagePlaceHolder, ex);
    }
}

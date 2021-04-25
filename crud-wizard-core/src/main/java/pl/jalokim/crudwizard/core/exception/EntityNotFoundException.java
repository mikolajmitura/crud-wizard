package pl.jalokim.crudwizard.core.exception;

import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class EntityNotFoundException extends ApplicationException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(MessagePlaceholder messagePlaceHolder) {
        super(messagePlaceHolder);
    }

    public EntityNotFoundException(MessagePlaceholder messagePlaceHolder, Throwable ex) {
        super(messagePlaceHolder, ex);
    }
}

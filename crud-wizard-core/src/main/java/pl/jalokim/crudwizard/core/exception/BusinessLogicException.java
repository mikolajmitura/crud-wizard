package pl.jalokim.crudwizard.core.exception;

import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class BusinessLogicException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String message, Throwable ex) {
        super(message, ex);
    }

    public BusinessLogicException(MessagePlaceholder messagePlaceHolder) {
        super(messagePlaceHolder);
    }

    public BusinessLogicException(MessagePlaceholder messagePlaceHolder, Throwable ex) {
        super(messagePlaceHolder, ex);
    }
}

package pl.jalokim.crudwizard.core.exception;

import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public class TechnicalException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public TechnicalException(String message) {
        super(message);
    }

    public TechnicalException(String message, Throwable ex) {
        super(message, ex);
    }

    public TechnicalException(MessagePlaceholder messagePlaceHolder) {
        super(messagePlaceHolder);
    }

    public TechnicalException(MessagePlaceholder messagePlaceHolder, Throwable ex) {
        super(messagePlaceHolder, ex);
    }
}

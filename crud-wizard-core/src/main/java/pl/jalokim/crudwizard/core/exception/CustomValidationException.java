package pl.jalokim.crudwizard.core.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.util.Set;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

@Getter
public class CustomValidationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    private final Set<ErrorWithMessagePlaceholder> errors;
    private final HttpStatus statusCode;

    public CustomValidationException(String message, Set<ErrorWithMessagePlaceholder> errors) {
        super(message);
        this.errors = errors;
        statusCode = BAD_REQUEST;
    }

    public CustomValidationException(MessagePlaceholder messagePlaceHolder, Set<ErrorWithMessagePlaceholder> errors) {
        super(messagePlaceHolder);
        this.errors = errors;
        statusCode = BAD_REQUEST;
    }

    public CustomValidationException(String message, Set<ErrorWithMessagePlaceholder> errors, HttpStatus statusCode) {
        super(message);
        this.errors = errors;
        this.statusCode = statusCode;
    }

    public CustomValidationException(MessagePlaceholder messagePlaceHolder, Set<ErrorWithMessagePlaceholder> errors,
        HttpStatus statusCode) {
        super(messagePlaceHolder);
        this.errors = errors;
        this.statusCode = statusCode;
    }
}

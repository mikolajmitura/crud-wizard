package pl.jalokim.crudwizard.core.exception;

import java.util.Set;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

@Builder(toBuilder = true)
@Value
public class ErrorWithPlaceholders {

    MessagePlaceholder messagePlaceholder;
    String property;
    String rawMessage;
    String errorCode;
    Set<ErrorWithPlaceholders> errors;

    public static ErrorWithPlaceholders newEntry(String property, String errorCode, String message) {
        return ErrorWithPlaceholders.builder()
            .property(property)
            .errorCode(errorCode)
            .rawMessage(message)
            .build();
    }
}

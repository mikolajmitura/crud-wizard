package pl.jalokim.crudwizard.core.exception;

import java.util.Set;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

@Builder
@Value
public class ErrorWithMessagePlaceholder {

    MessagePlaceholder messagePlaceholder;
    String property;
    String rawMessage;
    String errorCode;
    Set<ErrorWithMessagePlaceholder> errors;
}

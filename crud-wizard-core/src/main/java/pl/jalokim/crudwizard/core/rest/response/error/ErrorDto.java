package pl.jalokim.crudwizard.core.rest.response.error;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder;

@Builder
@Value
public class ErrorDto {

    String property;
    String message;
    String errorCode;

    public static ErrorDto errorEntry(String property, String message) {
        return ErrorDto.builder()
            .property(property)
            .message(message)
            .build();
    }

    public static ErrorDto errorEntry(String property, String message, String errorCode) {
        return ErrorDto.builder()
            .property(property)
            .message(message)
            .errorCode(errorCode)
            .build();
    }

    public static ErrorDto errorEntryWithErrorCode(String property, String translationKey) {
        return ErrorDto.builder()
            .property(property)
            .message(AppMessageSourceHolder.getMessage(translationKey))
            .errorCode(translationKey)
            .build();
    }

    @Override
    public String toString() {
        return "ErrorDto{" +
            "property='" + property + '\'' +
            ", message='" + message + '\'' +
            ", errorCode='" + errorCode + '\'' +
            "}\n";
    }
}

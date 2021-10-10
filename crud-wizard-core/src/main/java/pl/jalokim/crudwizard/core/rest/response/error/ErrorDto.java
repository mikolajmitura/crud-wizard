package pl.jalokim.crudwizard.core.rest.response.error;

import lombok.Builder;
import lombok.Value;

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
}

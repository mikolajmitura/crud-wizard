package pl.jalokim.crudwizard.core.rest.response.error;

import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class ErrorResponseDto {

    String errorCode;
    String message;
    Set<ErrorDto> errors;
}

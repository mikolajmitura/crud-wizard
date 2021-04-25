package pl.jalokim.crudwizard.core.exception.handler;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.exception.ErrorWithMessagePlaceholder;
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

@UtilityClass
public class ErrorWithMessagePlaceholderMapper {

    public static Set<ErrorDto> convertToErrorsDto(Set<ErrorWithMessagePlaceholder> errorsDtoWithPlaceholder) {
        if (errorsDtoWithPlaceholder == null) {
            return null;
        }
        return errorsDtoWithPlaceholder.stream()
            .map(errorDtoWithPlaceholder ->
                ErrorDto.builder()
                    .property(errorDtoWithPlaceholder.getProperty())
                    .errorCode(Optional.ofNullable(errorDtoWithPlaceholder.getErrorCode())
                        .orElse(Optional.ofNullable(errorDtoWithPlaceholder.getMessagePlaceholder())
                            .map(MessagePlaceholder::getErrorCode)
                            .orElse(null))
                    )
                    .message(Optional.ofNullable(errorDtoWithPlaceholder.getRawMessage())
                        .orElse(Optional.ofNullable(errorDtoWithPlaceholder.getMessagePlaceholder())
                            .map(MessagePlaceholder::translateMessage)
                            .orElse(null))
                    )
                    .build()
            )
            .collect(Collectors.toUnmodifiableSet());
    }
}

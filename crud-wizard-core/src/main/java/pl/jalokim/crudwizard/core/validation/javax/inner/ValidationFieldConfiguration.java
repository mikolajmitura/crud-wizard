package pl.jalokim.crudwizard.core.validation.javax.inner;

import java.lang.annotation.Annotation;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;

@Builder
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationFieldConfiguration {

    String fieldByPositionName;
    String fieldByPositionValue;
    String expectedFieldStateFieldName;
    ExpectedFieldState expectedFieldState;
    String otherFieldValueName;
    List<String> otherFieldValue;
    Class<? extends Annotation> annotationType;
}

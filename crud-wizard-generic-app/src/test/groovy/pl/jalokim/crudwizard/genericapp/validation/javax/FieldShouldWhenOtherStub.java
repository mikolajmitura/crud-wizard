package pl.jalokim.crudwizard.genericapp.validation.javax;

import lombok.Builder;
import lombok.Getter;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;

@Builder
@Getter
public class FieldShouldWhenOtherStub {
    String field;
    ExpectedFieldState should;
    @Builder.Default
    String[] fieldValues = {};
    String whenField;
    ExpectedFieldState is;
    @Builder.Default
    String[] otherFieldValues = {};
}

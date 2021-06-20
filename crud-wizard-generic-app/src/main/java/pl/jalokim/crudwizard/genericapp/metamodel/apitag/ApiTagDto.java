package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ObjectWithVersion;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@EqualsAndHashCode(callSuper = true)
@Value
@FieldShouldWhenOther(
    field = "id",
    should = NOT_NULL,
    whenField = "name",
    is = NULL
)
@FieldShouldWhenOther(
    field = "name",
    should = NOT_NULL,
    whenField = "id",
    is = NULL
)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ApiTagDto extends ObjectWithVersion {

    Long id;

    String name;
}
package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.ObjectWithVersionDto;

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
@Jacksonized
@SuperBuilder(toBuilder = true)
public class ApiTagDto extends ObjectWithVersionDto {

    Long id;

    // TODO #44 verify that name is unique when create
    String name;
}

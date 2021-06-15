package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.BaseModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

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
public class ApiTagDto extends BaseModelDto {

    Long id;

    String name;
}

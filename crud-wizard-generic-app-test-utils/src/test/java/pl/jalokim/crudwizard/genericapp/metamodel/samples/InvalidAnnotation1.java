package pl.jalokim.crudwizard.genericapp.metamodel.samples;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;

import lombok.Data;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@Data
@FieldShouldWhenOther(field = "name", should = NOT_NULL)
public class InvalidAnnotation1 {

    private String name;
}

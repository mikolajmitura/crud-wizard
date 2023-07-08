package pl.jalokim.crudwizard.genericapp.metamodel.samples;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;

import lombok.Data;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@Data
@FieldShouldWhenOther(field = "name", should = NOT_NULL, whenField="name2")
public class InvalidAnnotation2 {

    private String name;
    private String name2;
}

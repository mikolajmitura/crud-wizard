package pl.jalokim.crudwizard.genericapp.validation.javax;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldShouldWhenOther.List.class)
@Documented
@Constraint(validatedBy = FieldShouldWhenOtherValidator.class)
public @interface FieldShouldWhenOther {

    String DEFAULT_MESSAGE = "{pl.jalokim.validation.javax.FieldShouldWhenOther.message}";

    String field();

    OtherFieldMatch should();

    String[] fieldValues() default {};

    String whenField();

    OtherFieldMatch is();

    String[] otherFieldValues() default {};

    String message() default DEFAULT_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        FieldShouldWhenOther[] value();
    }

}

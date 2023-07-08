package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.UNKNOWN;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldShouldWhenOther.List.class)
@Documented
@Constraint(validatedBy = FieldShouldWhenOtherJavaxValidator.class)
public @interface FieldShouldWhenOther {

    String NOT_PROVIDED_VALUE = "<NOT_PROVIDED>";

    String field();

    ExpectedFieldState should();

    String[] fieldValues() default {};

    String whenField() default NOT_PROVIDED_VALUE;

    ExpectedFieldState is() default UNKNOWN;

    String[] otherFieldValues() default {};

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        FieldShouldWhenOther[] value();
    }

}

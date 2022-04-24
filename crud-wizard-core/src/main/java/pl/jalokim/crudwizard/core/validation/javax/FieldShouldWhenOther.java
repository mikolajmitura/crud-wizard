package pl.jalokim.crudwizard.core.validation.javax;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import pl.jalokim.crudwizard.core.validation.SomeAnnotation;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldShouldWhenOther.List.class)
@Documented
@Constraint(validatedBy = FieldShouldWhenOtherJavaxValidator.class)
public @interface FieldShouldWhenOther {

    String field();

    ExpectedFieldState should();

    String[] fieldValues() default {};

    String whenField();

    ExpectedFieldState is();

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

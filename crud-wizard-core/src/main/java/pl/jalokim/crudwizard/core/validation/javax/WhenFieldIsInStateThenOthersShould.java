package pl.jalokim.crudwizard.core.validation.javax;

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
@Repeatable(WhenFieldIsInStateThenOthersShould.List.class)
@Documented
@Constraint(validatedBy = FieldsShouldOnlyWhenValidator.class)
public @interface WhenFieldIsInStateThenOthersShould {

    String whenField();

    ExpectedFieldState is();

    String[] fieldValues() default {};

    FieldShouldWhenOther[] thenOthersShould() default {};

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        WhenFieldIsInStateThenOthersShould[] value();
    }
}

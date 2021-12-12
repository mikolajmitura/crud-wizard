package pl.jalokim.crudwizard.core.validation.javax;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ClassExistsValidator.class)
public @interface ClassExists {

    String message() default "";

    Class<?>[] groups() default {};

    Class<?> expectedOfType() default Object.class;

    boolean canBeAbstractOrInterface() default false;

    Class<? extends Payload>[] payload() default {};
}

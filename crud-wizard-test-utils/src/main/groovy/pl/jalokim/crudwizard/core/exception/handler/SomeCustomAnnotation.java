package pl.jalokim.crudwizard.core.exception.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import pl.jalokim.crudwizard.core.translations.ExampleEnum;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SomeCustomAnnotationValidator.class)
public @interface SomeCustomAnnotation {

    ExampleEnum exampleEnum();

    String message() default "{SomeCustomValidation.some.validation.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

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
@Constraint(
    validatedBy = {}
)
public @interface FieldShouldWhenOther {

    String field();

    OtherFieldMatch shouldBe = OtherFieldMatch.NULL;

    String[] shouldHaveValue() default {};

    String whenField();

    OtherFieldMatch is = OtherFieldMatch.NOT_NULL;

    String[] otherFieldValues() default {};

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FieldShouldWhenOther[] value();
    }

}

package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(BeforeValidationInvoke.List.class)
public @interface BeforeValidationInvoke {

    Class<?> beanType();

    String methodName();

    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        BeforeValidationInvoke[] value();
    }
}

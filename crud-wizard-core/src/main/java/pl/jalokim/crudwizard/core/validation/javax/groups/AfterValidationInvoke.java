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
@Repeatable(AfterValidationInvoke.List.class)
public @interface AfterValidationInvoke {

    Class<?> beanType();

    /**
     * given method name should contains method argument with type {@link ValidationResult}
     * or
     * first argument is for validated object and second argument is type of {@link ValidationResult}
     */
    String methodName();

    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        AfterValidationInvoke[] value();
    }
}

package pl.jalokim.crudwizard.core.validation.javax.groups;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;

@Aspect
@Component
@AllArgsConstructor
public class ValidatedInServiceInterceptor {

    private final ValidatorFactory validatorFactory;
    private final ApplicationContext applicationContext;

    /**
     * When some argument should be validated in some spring bean then method should be annotated with {@link org.springframework.validation.annotation.Validated}
     * And some parameter objects should be annotated with {@link org.springframework.validation.annotation.Validated}
     */
    @Around("@annotation(org.springframework.validation.annotation.Validated)")
    public Object validateBeanWhenShould(ProceedingJoinPoint pjp) throws Throwable {
        if (pjp.getSignature().getClass().getCanonicalName().contains("MethodSignature")) {
            Method invokeMethod = InvokableReflectionUtils.invokeMethod(pjp.getSignature(), "getMethod");
            Annotation[][] parameterAnnotations = invokeMethod.getParameterAnnotations();
            for (int parameterIndex = 0; parameterIndex < parameterAnnotations.length; parameterIndex++) {
                Annotation[] parameterAnnotation = parameterAnnotations[parameterIndex];

                validateBeanWhenShould(pjp, parameterIndex, parameterAnnotation);
            }
        }

        return pjp.proceed();
    }

    private void validateBeanWhenShould(ProceedingJoinPoint pjp, int parameterIndex, Annotation... parameterAnnotation) {

        Validated foundValidated = null;
        List<BeforeValidationInvoke> foundBeforeValidationToInvoke = new ArrayList<>();

        for (Annotation annotation : parameterAnnotation) {
            if (annotation.annotationType().equals(Validated.class)) {
                foundValidated = (Validated) annotation;
            }

            if (annotation.annotationType().equals(BeforeValidationInvoke.class)) {
                foundBeforeValidationToInvoke.add((BeforeValidationInvoke) annotation);
            }

            if (annotation.annotationType().equals(BeforeValidationInvoke.List.class)) {
                BeforeValidationInvoke.List beforeValidationInvokeList = (BeforeValidationInvoke.List) annotation;
                BeforeValidationInvoke[] value = beforeValidationInvokeList.value();
                foundBeforeValidationToInvoke.addAll(elements(value).asList());
            }
        }

        var methodArguments = pjp.getArgs();
        foundBeforeValidationToInvoke.forEach(foundBeforeValidationInvoke -> {
            Object instance = applicationContext.getBean(foundBeforeValidationInvoke.beanType());
            InvokableReflectionUtils.invokeMethod(instance, foundBeforeValidationInvoke.methodName(), methodArguments[parameterIndex]);
        });

        if (foundValidated != null) {
            Class<?>[] groups = foundValidated.value();
            ValidationUtils.validateBean(validatorFactory.getValidator(), methodArguments[parameterIndex], groups);
        }
    }
}

package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;

@Aspect
@Component
@AllArgsConstructor
public class ValidatedInServiceInterceptor {

    private final ValidatorFactory validatorFactory;

    @Around("bean(*Service) || bean(*ServiceImpl)")
    public Object validateBeanWhenShould(ProceedingJoinPoint pjp) throws Throwable {
        if (pjp.getSignature().getClass().getCanonicalName().contains("MethodSignature")) {
            Method invokeMethod = InvokableReflectionUtils.invokeMethod(pjp.getSignature(), "getMethod");
            Annotation[][] parameterAnnotations = invokeMethod.getParameterAnnotations();
            for (int parameterIndex = 0; parameterIndex < parameterAnnotations.length; parameterIndex++) {
                Annotation[] parameterAnnotation = parameterAnnotations[parameterIndex];
                for (Annotation annotation : parameterAnnotation) {
                    if (annotation.annotationType().equals(Validated.class)) {
                        var methodArguments = pjp.getArgs();
                        Class<?>[] groups = ((Validated) annotation).value();
                        ValidatorFactoryHolder.validateBean(validatorFactory.getValidator(), methodArguments[parameterIndex], groups);
                    }
                }
            }
        }

        return pjp.proceed();
    }
}

package pl.jalokim.crudwizard.genericapp.validation.validator.inner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnnotationInvocationHandler implements InvocationHandler {

    private final Map<String, Object> returnValueByMethodName;
    private final Class<? extends Annotation> targetAnnotationType;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Object returnValue = returnValueByMethodName.get(method.getName());
        if (returnValue == null) {
            throw new IllegalArgumentException("Cannot invoke method: " + method.getName() + " at class: " + targetAnnotationType.getCanonicalName() +
                " due to cannot find return value for that method available: " + returnValueByMethodName);
        }
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createAnnotationInstance(Class<T> annotationClass, Map<String, Object> returnValueByMethodName) {
        return (T) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{annotationClass},
            new AnnotationInvocationHandler(returnValueByMethodName, annotationClass));
    }
}

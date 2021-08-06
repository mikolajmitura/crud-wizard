package pl.jalokim.crudwizard.core.utils;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.utils.reflection.ReflectionOperationException;

@UtilityClass
public class ReflectionUtils {

    public static Object invokeMethod(Object target, Method method, Object... methodArgs) {
        try {
            return method.invoke(target, methodArgs);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new ReflectionOperationException("Cannot invoke method", e);
        }
    }

    public static Method findMethodByName(Class<?> fromClass, String methodName) {
        var foundMethods = elements(fromClass.getMethods())
            .filter(method -> method.getName().equals(methodName))
            .asList();

        if (foundMethods.isEmpty()) {
            throw new TechnicalException(String.format("Cannot find method with name: '%s' in class %s",
                methodName, fromClass.getCanonicalName()));
        }

        if (foundMethods.size() > 1) {
            String foundMethodsAsText = elements(foundMethods).asConcatText(System.lineSeparator());

            throw new TechnicalException(String.format("Found more than one method with name '%s' in class: %s found methods: %s",
                methodName, fromClass.getCanonicalName(), foundMethodsAsText));
        }

        return elements(foundMethods).getFirst();
    }
}

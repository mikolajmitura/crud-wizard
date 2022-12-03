package pl.jalokim.crudwizard.core.utils;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllNotStaticMethods;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isPublicMethod;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isStaticMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.exception.TechnicalException;

@UtilityClass
public class ReflectionUtils {

    public static Method findMethodByName(Class<?> fromClass, String methodName) {
        var foundMethods = elements(getAllNotStaticMethods(fromClass))
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

    public static boolean methodReturnsNonVoidAndHasArgumentsSize(Method method, int argumentsNumber) {
        return method.getParameterCount() == argumentsNumber &&
            !method.getReturnType().equals(void.class) &&
            !method.getReturnType().equals(Void.class);
    }

    public static boolean methodReturnsVoidAndHasArgumentsSize(Method method, int argumentsNumber) {
        return method.getParameterCount() == argumentsNumber &&
            (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class));
    }

    public static boolean hasBuilderMethod(Class<?> someClass) {
        try {
            Method builderMethod = someClass.getDeclaredMethod("builder");
            return methodReturnsNonVoidAndHasArgumentsSize(builderMethod, 0) &&
                isStaticMethod(builderMethod) && isPublicMethod(builderMethod);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean hasOnlyDefaultConstructor(Class<?> someClass) {
        Constructor<?>[] declaredConstructors = someClass.getConstructors();
        return declaredConstructors.length == 1 && declaredConstructors[0].getParameterCount() == 0;
    }

    public static boolean hasOneConstructorMaxArgNumbers(Class<?> someClass) {
        return findOneConstructorMaxArgNumbers(someClass) != null;
    }

    public static Constructor<?> findOneConstructorMaxArgNumbers(Class<?> someClass) {
        Constructor<?>[] constructors = someClass.getConstructors();
        int maxArgsFound = 0;
        List<Constructor<?>> foundConstructors = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (maxArgsFound < constructor.getParameterCount()) {
                maxArgsFound = constructor.getParameterCount();
                foundConstructors.clear();
                foundConstructors.add(constructor);
            } else if (maxArgsFound == constructor.getParameterCount()) {
                foundConstructors.add(constructor);
            }
        }
        if (foundConstructors.size() == 1) {
            return foundConstructors.get(0);
        }
        return null;
    }
}

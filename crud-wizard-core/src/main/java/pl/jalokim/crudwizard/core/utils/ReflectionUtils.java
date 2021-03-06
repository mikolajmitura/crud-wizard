package pl.jalokim.crudwizard.core.utils;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllNotStaticMethods;

import java.lang.reflect.Method;
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
}

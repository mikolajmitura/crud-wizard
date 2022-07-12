package pl.jalokim.crudwizard.core.utils;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getClassForName;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ReflectionOperationException;

@UtilityClass
public class ClassUtils {

    public static Class<?> loadRealClass(String className) {
        return Optional.ofNullable(className)
            .map(ClassUtils::clearCglibClassName)
            .map(MetadataReflectionUtils::getClassForName)
            .orElse(null);
    }

    public static boolean checkThatClassExists(String nullableClassName) {
        return Optional.ofNullable(nullableClassName)
            .map(ClassUtils::clearCglibClassName)
            .map(className -> {
                try {
                    getClassForName(className);
                    return true;
                } catch (ReflectionOperationException ex) {
                    return false;
                }
            }).orElse(false);
    }

    public static Class<?> loadRealClass(Class<?> classProxiedByCglib) {
        return loadRealClass(clearCglibClassName(classProxiedByCglib.getCanonicalName()));
    }

    public static String clearCglibClassName(String className) {
        String clearClassName = className;
        if (className.contains("$$EnhancerBySpringCGLIB")) {
            // does not support inner classes names
            clearClassName = clearClassName.split("\\$\\$EnhancerBySpringCGLIB")[0];
        }
        return clearClassName;
    }
}

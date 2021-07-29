package pl.jalokim.crudwizard.core.utils;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@UtilityClass
public class ClassUtils {

    public static Class<?> loadRealClass(String className) {
        return Optional.ofNullable(className)
            .map(ClassUtils::clearCglibClassName)
            .map(MetadataReflectionUtils::getClassForName)
            .orElse(null);
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

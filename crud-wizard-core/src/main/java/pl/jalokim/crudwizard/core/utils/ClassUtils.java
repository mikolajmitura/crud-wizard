package pl.jalokim.crudwizard.core.utils;

import java.util.Optional;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

public class ClassUtils {

    public static Class<?> loadRealClass(String className) {
        return Optional.ofNullable(className)
            .map(MetadataReflectionUtils::getClassForName)
            .orElse(null);
    }
}

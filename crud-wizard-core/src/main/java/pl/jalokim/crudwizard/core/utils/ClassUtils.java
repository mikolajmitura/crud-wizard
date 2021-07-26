package pl.jalokim.crudwizard.core.utils;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@UtilityClass
public class ClassUtils {

    public static Class<?> loadRealClass(String className) {
        return Optional.ofNullable(className)
            .map(MetadataReflectionUtils::getClassForName)
            .orElse(null);
    }
}

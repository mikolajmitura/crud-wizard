package pl.jalokim.crudwizard.core.translations;

import static java.util.Objects.isNull;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.NoSuchMessageException;

public interface AppMessageSource {

    String getMessage(String propertyKey);

    default String getMessage(Class<?> classNameAsPrefix, String code, Object... placeholderArgs) {
        String fullPropertyKey = buildPropertyKey(getClassName(classNameAsPrefix), code);
        if (placeholderArgs.length > 0) {
            return getMessage(fullPropertyKey, placeholderArgs);
        }
        return getMessage(fullPropertyKey);
    }

    default String getMessage(String propertyKey, Object... placeholderArgs) {
        return MessageFormat.format(getMessage(propertyKey), placeholderArgs);
    }

    default String getMessageByEnum(Enum<?> enumValue, Object... placeholderArgs) {
        if (isNull(enumValue)) {
            return null;
        }
        return getMessage(enumValue.getClass(), enumValue.name(), placeholderArgs);
    }

    default String getMessageWithPrefix(String prefix, String propertyKey, Object... placeholderArgs) {
        return getMessage(buildPropertyKey(prefix, propertyKey), placeholderArgs);
    }

    default String getMessageByEnumWithPrefix(String prefix, Enum<?> enumValue, Object... placeholderArgs) {
        return getMessageWithPrefix(prefix, buildPropertyKey(enumValue), placeholderArgs);
    }

    default String getMessageWithOptionalSuffixes(String rootPropertyKey, Object... optionalSuffixes) {
        if (isNull(rootPropertyKey)) {
            return null;
        }
        List<Object> propertyKeyParts = Stream.concat(Stream.of(rootPropertyKey), Arrays.stream(optionalSuffixes))
            .collect(Collectors.toList());
        while (propertyKeyParts.size() > 1) {
            String propertyKeyWithSuffix = buildPropertyKey(propertyKeyParts.toArray());
            propertyKeyParts.remove(propertyKeyParts.size() - 1);
            try {
                return getMessage(propertyKeyWithSuffix);
            } catch (NoSuchMessageException e) {
                // try lower property key
            }
        }
        return getMessage(rootPropertyKey);
    }

    default String getMessageByEnumWithSuffixes(Enum<?> enumValue, Object... optionalSuffixes) {
        return getMessageWithOptionalSuffixes(buildPropertyKey(enumValue), optionalSuffixes);
    }

    static String buildPropertyKey(Enum<?> enumValue) {
        return buildPropertyKey(getClassName(enumValue.getClass()), enumValue);
    }

    static String buildPropertyKey(Class<?> classType, Object... propertyKeyParts) {
        return buildPropertyKey(Stream.concat(Stream.of(getClassName(classType)), Arrays.stream(propertyKeyParts)).toArray());
    }

    static String buildPropertyKey(Object... propertyKeyParts) {
        return Arrays.stream(propertyKeyParts)
            .map(Object::toString)
            .collect(Collectors.joining("."));
    }

    static String getClassName(Class<?> classType) {
        return classType.getCanonicalName();
    }
}

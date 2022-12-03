package pl.jalokim.crudwizard.core.translations;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static pl.jalokim.crudwizard.core.config.jackson.EnumToLowerCaseSerializer.enumAsLowerCase;
import static pl.jalokim.crudwizard.core.translations.AppMessageSource.buildPropertyKey;
import static pl.jalokim.crudwizard.core.translations.AppMessageSource.getClassName;
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * To use that class necessary is to have bean {@link AppMessageSource} in spring context and in static holder that bean should be added in static holder {@link
 * AppMessageSourceHolder}
 */
@Builder(toBuilder = true)
@Value
public class MessagePlaceholder {

    String mainPlaceholder;
    List<Object> rawArguments;
    String errorCode;
    Map<String, Object> rawArgumentsByName;

    @Override
    public String toString() {
        return translateMessage();
    }

    /**
     * Will create MessagePlaceholder for message key which as prefix has canonical class name + propertySuffix and all given placeholderArgs will try wrap as
     * placeholder or try translate:
     * <ul>
     *  <li> so for example enum pl.jalokim.package.SomeEnum.ENTRY will be wrapped as {pl.jalokim.package.SomeEnum.ENTRY} </li>
     *  <li> if some argument looks like property e.g. 'some.property.key' thn will be {some.property.key} </li>
     *  <li> if argument is instance of {@link #MessagePlaceholder} then method {@link #translateMessage()} will be invoked on it. </li>
     *  <li> rest of arguments will be passed as normal arguments so {@link Object#toString()}} will be invoked on them to populate message arguments. </li>
     * </ul>
     *
     * @param classNameAsPrefix - this property and propertySuffix will be concatendated as one property e.g. classNameAsPrefix is java.lang.Object and
     * propertySuffix is 'some.code' will be genrated property java.lang.Object.some.code
     * @param propertySuffix - suffix part of property key
     * @param placeholderArgs - arguments for populate message arguments for example there is some.property=some message {0} {1} {3} so variables passed as
     * placeholderArgs will populate placehoders in above message.
     * @return instance of MessagePlaceholder
     */
    public static MessagePlaceholder createMessagePlaceholder(Class<?> classNameAsPrefix, String propertySuffix, Object... placeholderArgs) {
        return MessagePlaceholder.builder()
            .mainPlaceholder(classNameAsPrefix, propertySuffix)
            .argumentsWithAutoTranslate(placeholderArgs)
            .build();
    }

    public static MessagePlaceholder createMessagePlaceholder(Class<?> classNameAsPrefix, String propertySuffix, Map<String, Object> argumentsByName) {
        return MessagePlaceholder.builder()
            .mainPlaceholder(classNameAsPrefix, propertySuffix)
            .argumentsWithAutoTranslate(argumentsByName)
            .build();
    }

    /**
     * see description in {@link #createMessagePlaceholder(Class, String, Object...)}}
     *
     * @param propertyKey - property key which will be used for translation.
     * @param placeholderArgs - arguments for populate message arguments for example there is some.property=some message {0} {1} {3} so variables passed as
     * placeholderArgs will populate placehoders in above message.
     * @return instance of MessagePlaceholder
     */
    public static MessagePlaceholder createMessagePlaceholder(String propertyKey, Object... placeholderArgs) {
        return MessagePlaceholder.builder()
            .mainPlaceholder(propertyKey)
            .argumentsWithAutoTranslate(placeholderArgs)
            .build();
    }

    public static MessagePlaceholder createMessagePlaceholder(String propertyKey, Map<String, Object> argumentsByName) {
        return MessagePlaceholder.builder()
            .mainPlaceholder(propertyKey)
            .argumentsWithAutoTranslate(argumentsByName)
            .build();
    }

    public static MessagePlaceholder createMessagePlaceholderWithKey(String propertyKey, String placeholderKey, Object placeholderValue) {
        return createMessagePlaceholder(propertyKey, Map.of(placeholderKey, placeholderValue));
    }

    public static String translatePlaceholder(String propertyKey, Object... placeholderArgs) {
        return createMessagePlaceholder(propertyKey, placeholderArgs).translateMessage();
    }

    public static String translatePlaceholder(String propertyKey, Map<String, Object> placeholderArgs) {
        return createMessagePlaceholder(propertyKey, placeholderArgs).translateMessage();
    }

    /**
     * placeholder which should be translated at frontend side
     *
     * @param textToWrap value to wrap
     * @return it returns F#[$textToWrap]#
     */
    public static String wrapAsExternalPlaceholder(String textToWrap) {
        return String.format("F#[%s]#", textToWrap);
    }

    /**
     * placeholder for enum which should be translated at frontend side
     *
     * @param enumToWrap value to wrap
     * @return it returns F_Enum#[$enumType.$textToWrap]#
     */
    public static String wrapAsExternalPlaceholder(Enum<?> enumToWrap) {
        return String.format("F_Enum#[%s.%s]#",
            UPPER_CAMEL.to(LOWER_UNDERSCORE, enumToWrap.getClass().getSimpleName()),
            enumAsLowerCase(enumToWrap));
    }

    public static String wrapAsPlaceholder(Object... propertyKeyParts) {
        return String.format("{%s}", buildPropertyKey(propertyKeyParts));
    }

    public static String wrapAsPlaceholder(Class<?> classNameAsPrefix, String code) {
        return wrapAsPlaceholder(getClassName(classNameAsPrefix), code);
    }

    public static String wrapAsPlaceholder(Class<?> classNameAsPrefix) {
        return wrapAsPlaceholder(getClassName(classNameAsPrefix));
    }

    public static String wrapAsPlaceholder(Enum<?> enumValue) {
        return wrapAsPlaceholder(buildPropertyKey(enumValue));
    }

    public String translateMessage() {
        if (rawArgumentsByName != null) {
            Map<String, Object> translated = rawArgumentsByName.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), translateObjectWhenIsPlaceholder(entry.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            return getAppMessageSource().getMessage(mainPlaceholder, translated);
        }
        Object[] translatedArguments = rawArguments.stream()
            .map(MessagePlaceholder::translateObjectWhenIsPlaceholder)
            .toArray();
        return getAppMessageSource().getMessage(mainPlaceholder, translatedArguments);
    }

    static Object translateObjectWhenIsPlaceholder(Object textOrPlaceholder) {
        return translateWhenIsPlaceholder(textOrPlaceholder);
    }

    public static Object translateWhenIsPlaceholder(Object textOrPlaceholder, Object... rawArguments) {
        if (textOrPlaceholder instanceof String) {
            String messageOrPlaceholderAsText = (String) textOrPlaceholder;
            if (hasPlaceholderFormat(messageOrPlaceholderAsText)) {
                return getAppMessageSource().getMessage(extractPropertyKey(messageOrPlaceholderAsText), rawArguments);
            }
        }

        return textOrPlaceholder;
    }

    public static boolean hasPlaceholderFormat(String messageOrPlaceholder) {
        return messageOrPlaceholder.matches("^\\{(.)+\\}$");
    }

    public static String extractPropertyKey(String placeholder) {
        return placeholder
            .replace("{", StringUtils.EMPTY)
            .replace("}", StringUtils.EMPTY)
            .trim();
    }

    static Stream<Object> tryWrapAllAsPlaceholderOrTranslate(Object... arguments) {
        return Arrays.stream(arguments)
            .map(MessagePlaceholder::tryWrapAsPlaceholderOrTranslate);
    }

    static Object tryWrapAsPlaceholderOrTranslate(Object argument) {
        if (argument instanceof Enum) {
            return wrapAsPlaceholder((Enum<?>) argument);
        }
        if (argument instanceof MessagePlaceholder) {
            return ((MessagePlaceholder) argument).translateMessage();
        }
        return argument;
    }

    public static class MessagePlaceholderBuilder {

        private MessagePlaceholderBuilder() {
            rawArguments = new ArrayList<>();
        }

        public MessagePlaceholderBuilder mainPlaceholder(String mainPlaceholder) {
            this.mainPlaceholder = mainPlaceholder;
            return this;
        }

        public MessagePlaceholderBuilder mainPlaceholder(Enum<?> enumValue) {
            return mainPlaceholder(buildPropertyKey(enumValue));
        }

        public MessagePlaceholderBuilder mainPlaceholder(Class<?> classAsPrefix, String code) {
            return mainPlaceholder(buildPropertyKey(classAsPrefix, code));
        }

        /**
         * All arguments will be wrapped as text placeholder, so next those property key will be translated to message.
         */
        public MessagePlaceholderBuilder argumentsAsPlaceholders(String... argumentsWithPlaceholder) {
            return rawArguments(Arrays.stream(
                argumentsWithPlaceholder)
                .map(MessagePlaceholder::wrapAsPlaceholder)
                .toArray());
        }

        /**
         * All data will not to be translated. But when you put here value like '{some.property}' then this property will be translated.
         */
        public MessagePlaceholderBuilder rawArguments(Object... rawArguments) {
            this.rawArguments.addAll(Arrays.asList(rawArguments));
            return this;
        }

        /**
         * All data will be tried to wrap as placeholder or translated automatically. So if text looks like property key then will be wrap as placeholder, when
         * is Enum value then will be wrapped as placeholder, when is instance of MessagePlaceholder then will be automatically translated.
         */
        public MessagePlaceholderBuilder argumentsWithAutoTranslate(Object... arguments) {
            return rawArguments(tryWrapAllAsPlaceholderOrTranslate(arguments).toArray());
        }

        /**
         * All data will be tried to wrap as placeholder or translated automatically. So if text looks like property key then will be wrap as placeholder, when
         * is Enum value then will be wrapped as placeholder, when is instance of MessagePlaceholder then will be automatically translated.
         */
        public MessagePlaceholderBuilder argumentsWithAutoTranslate(Map<String, Object> argumentsByName) {
            Map<String, Object> placeholdersOrTranslated = argumentsByName.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), tryWrapAsPlaceholderOrTranslate(entry.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            return rawArgumentsByName(placeholdersOrTranslated);
        }
    }
}

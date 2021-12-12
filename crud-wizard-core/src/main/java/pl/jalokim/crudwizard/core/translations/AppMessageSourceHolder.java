package pl.jalokim.crudwizard.core.translations;

import static pl.jalokim.crudwizard.core.translations.AppMessageSource.buildPropertyKey;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppMessageSourceHolder {

    private static final AtomicReference<AppMessageSource> APP_MESSAGE_SOURCE_INSTANCE = new AtomicReference<>();

    public static boolean existsAppMessageSource() {
        return APP_MESSAGE_SOURCE_INSTANCE.get() != null;
    }

    public static AppMessageSource getAppMessageSource() {
        return Optional.ofNullable(APP_MESSAGE_SOURCE_INSTANCE.get())
            .orElseThrow(() -> new NoSuchElementException("APP_MESSAGE_SOURCE_INSTANCE is not set"));
    }

    public static String getMessage(String propertyKey) {
        return getAppMessageSource().getMessage(propertyKey);
    }

    public static String getMessage(String propertyKey, Object... placeholderArgs) {
        return getAppMessageSource().getMessage(propertyKey, placeholderArgs);
    }

    public static String getMessage(Class<?> propertyKeyPrefix, String propertySuffix, Object... placeholderArgs) {
        return getAppMessageSource().getMessage(propertyKeyPrefix, propertySuffix, placeholderArgs);
    }

    public static String getMessage(Class<?> propertyKeyPrefix, String propertySuffix, Map<String, Object> placeholderArgsByName) {
        return getAppMessageSource().getMessage(buildPropertyKey(propertyKeyPrefix, propertySuffix), placeholderArgsByName);
    }

    public static String getMessage(String propertyKey, Map<String, Object> placeholderArgsByName) {
        return getAppMessageSource().getMessage(propertyKey, placeholderArgsByName);
    }

    public static String getMessageWithKey(String propertyKey, String placeholderKey, Object placeholderValue) {
        return getAppMessageSource().getMessage(propertyKey, Map.of(placeholderKey, placeholderValue.toString()));
    }

    static void setAppMessageSource(AppMessageSource appMessageSource) {
        APP_MESSAGE_SOURCE_INSTANCE.set(appMessageSource);
    }
}

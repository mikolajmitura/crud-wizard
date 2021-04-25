package pl.jalokim.crudwizard.core.translations;

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

    static void setAppMessageSource(AppMessageSource appMessageSource) {
        APP_MESSAGE_SOURCE_INSTANCE.set(appMessageSource);
    }
}

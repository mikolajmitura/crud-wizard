package pl.jalokim.crudwizard.core.translations;

import java.util.Locale;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocaleHolder {

    public static final String DEFAULT_LANG_CODE = "en_US";
    public static final String X_LOCALE_NAME_HEADER = "X-Locale";
    public static final ThreadLocal<Locale> LOCALE_REQUEST_HOLDER = new ThreadLocal<>();
    public static final Locale DEFAULT_LOCALE = new Locale("en", "US");

    public static Locale getLocale() {
        return Optional.ofNullable(LOCALE_REQUEST_HOLDER.get())
            .orElse(DEFAULT_LOCALE);
    }

    public static Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    public static void setLocale(Locale locale) {
        LOCALE_REQUEST_HOLDER.set(locale);
    }

    public static boolean hasSetLocale() {
        return LOCALE_REQUEST_HOLDER.get() != null;
    }

    public static void clear() {
        LOCALE_REQUEST_HOLDER.remove();
    }
}

package pl.jalokim.crudwizard.core.translations;

import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocaleUtils {

    public static Locale createLocale(String languageCode) {
        Locale locale;
        if (!isValidLocale(languageCode)) {
            throw new IllegalArgumentException("locale " + languageCode + " is invalid");
        }
        if (languageCode.contains("_")) {
            String[] codeParts = languageCode.split("_");
            locale = new Locale(codeParts[0], codeParts[1]);
        } else {
            locale = Locale.forLanguageTag(languageCode);
        }
        return locale;
    }

    boolean isValidLocale(String value) {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if (value.equals(locale.toString())) {
                return true;
            }
        }
        return false;
    }
}

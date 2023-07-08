package pl.jalokim.crudwizard.core.translations;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.MessageSource;

public interface RefreshableMessageSource extends MessageSource {

    Map<Locale, Map<String, String>> loadAndGetAllTranslations(List<Locale> allSupportedLocales);

    Map<Locale, Set<String>> refreshAndGetAllPropertiesKeysByLocale(List<Locale> allSupportedLocales);

    Map<Locale, Map<String, String>> getAllTranslationsPerLocale();

    String getTranslationSource();
}

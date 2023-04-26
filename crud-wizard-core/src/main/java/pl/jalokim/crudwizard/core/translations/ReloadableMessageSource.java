package pl.jalokim.crudwizard.core.translations;

import static java.util.ResourceBundle.getBundle;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@RequiredArgsConstructor
@Slf4j
public class ReloadableMessageSource extends MessageSourceFromMap {

    private final String translationName;

    @Override
    public Map<Locale, Map<String, String>> loadAndGetAllTranslations(List<Locale> allSupportedLocales) {
        Map<Locale, Map<String, String>> translationsByLocale = new HashMap<>();
        for (Locale locale : allSupportedLocales) {
            ResourceBundle bundle = getBundle(translationName, locale);
            if (bundle.getLocale().equals(locale) || locale.toString().equals(LocaleHolder.DEFAULT_LANG_CODE)) {
                List<String> keys = elements(bundle.getKeys().asIterator()).asList();
                if (CollectionUtils.isNotEmpty(keys)) {
                    Map<String, String> translations = new HashMap<>();
                    translationsByLocale.put(locale, translations);
                    for (String key : keys) {
                        translations.put(key, bundle.getString(key));
                    }
                }
            }
        }
        return Collections.unmodifiableMap(translationsByLocale);
    }

    @Override
    public String getTranslationSource() {
        return translationName;
    }
}

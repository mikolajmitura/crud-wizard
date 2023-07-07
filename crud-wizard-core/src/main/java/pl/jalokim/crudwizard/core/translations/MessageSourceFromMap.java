package pl.jalokim.crudwizard.core.translations;

import static pl.jalokim.utils.collection.Elements.elements;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.lang.Nullable;

public abstract class MessageSourceFromMap extends AbstractMessageSource implements RefreshableMessageSource {

    private final AtomicReference<Map<Locale, Map<String, String>>> translationsByLocaleRef = new AtomicReference<>(new HashMap<>());

    @Override
    public final Map<Locale, Set<String>> refreshAndGetAllPropertiesKeysByLocale(List<Locale> allSupportedLocales) {
        translationsByLocaleRef.set(loadAndGetAllTranslations(allSupportedLocales));
        return elements(translationsByLocaleRef.get().entrySet())
            .map(entry -> Pair.of(entry.getKey(), entry.getValue().keySet()))
            .asMap(Pair::getKey, Pair::getValue);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        return Optional.ofNullable(getTranslationByKey(code, locale))
            .map(message -> new MessageFormat(message, locale))
            .orElse(null);
    }

    @Nullable
    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return Optional.ofNullable(getTranslationByKey(code, locale))
            .orElse(null);
    }

    private String getTranslationByKey(String code, Locale locale) {
        Map<String, String> translationsByLocale = getTranslationsByLocale(locale);
        return translationsByLocale.get(code);
    }

    private Map<String, String> getTranslationsByLocale(Locale locale) {
        return translationsByLocaleRef.get().getOrDefault(locale, Map.of());
    }

    @Override
    public Map<Locale, Map<String, String>> getAllTranslationsPerLocale() {
        return translationsByLocaleRef.get();
    }

    @Override
    public abstract Map<Locale, Map<String, String>> loadAndGetAllTranslations(List<Locale> allSupportedLocales);
}

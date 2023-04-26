package pl.jalokim.crudwizard.core.translations;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageSourceProvider {

    @Getter
    private final RefreshableMessageSource messageSource;
    private final AtomicReference<Map<Locale, Set<String>>> propertiesByLocale = new AtomicReference<>();

    public boolean isSupportingLanguageAndProperty(Locale locale, String property) {
        return propertiesByLocale.get().get(locale) != null && propertiesByLocale.get().get(locale).contains(property);
    }

    public void refresh(List<Locale> allSupportedLocales) {
        setPropertiesByLocale(messageSource.refreshAndGetAllPropertiesKeysByLocale(allSupportedLocales));

    }

    protected void setPropertiesByLocale(Map<Locale, Set<String>> propertiesByLocale) {
        this.propertiesByLocale.set(propertiesByLocale);
    }
}

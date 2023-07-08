package pl.jalokim.crudwizard.genericapp.translation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.translations.MessageSourceFromMap;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationLanguageService;

@RequiredArgsConstructor
public class MessageSourceFromDb extends MessageSourceFromMap {

    private static final String FROM_DATA_BASE = "from data base";
    private final TranslationLanguageService translationLanguageService;

    @Override
    public Map<Locale, Map<String, String>> loadAndGetAllTranslations(List<Locale> allSupportedLocales) {
        return translationLanguageService.getAllTranslationsByLocales();
    }

    @Override
    public String getTranslationSource() {
        return FROM_DATA_BASE;
    }
}

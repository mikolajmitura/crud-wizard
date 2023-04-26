package pl.jalokim.crudwizard.genericapp.translation;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.translations.LocaleService;
import pl.jalokim.crudwizard.core.translations.LocaleUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationLanguageService;

@Service
@RequiredArgsConstructor
public class LocaleServiceImpl implements LocaleService {

    private final TranslationLanguageService translationLanguageService;

    @Override
    public List<Locale> getAllSupportedLocales() {
        return elements(translationLanguageService.getAllSupportedLanguages().keySet())
            .map(LocaleUtils::createLocale)
            .asList();
    }
}

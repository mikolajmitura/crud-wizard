package pl.jalokim.crudwizard.genericapp.metamodel.translation.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.translations.LocaleHolder;
import pl.jalokim.crudwizard.core.translations.LocaleUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.LanguageTranslationsDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationAndSourceDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class LanguageCanBeEnabledValidator implements BaseConstraintValidator<LanguageCanBeEnabled, LanguageTranslationsDto> {

    private final TranslationService translationService;

    @Override
    public boolean isValidValue(LanguageTranslationsDto languageTranslationsDto, ConstraintValidatorContext context) {
        String langCode = languageTranslationsDto.getLanguageCode();
        if (ObjectUtils.allNotNull(langCode,
            languageTranslationsDto.getLanguageFullName(),
            languageTranslationsDto.getEnabled(),
            languageTranslationsDto.getTranslations()) && languageTranslationsDto.getEnabled()) {

            var allRequiredTranslationKeys = elements(translationService
                .getTranslationsAndSourceByLocale(LocaleHolder.getDefaultLocale()))
                .map(TranslationAndSourceDto::getTranslationKey)
                .asList();

            List<String> alreadySavedTranslationKeys = elements(translationService
                .getTranslationsAndSourceByLocale(LocaleUtils.createLocale(langCode)))
                .map(TranslationAndSourceDto::getTranslationKey)
                .asList();
            alreadySavedTranslationKeys.addAll(languageTranslationsDto.getTranslations().keySet());

            List<String> notFoundTranslationKeys = new ArrayList<>();
            for (String requiredTranslationKey : allRequiredTranslationKeys) {
                if (!alreadySavedTranslationKeys.contains(requiredTranslationKey)) {
                    notFoundTranslationKeys.add(requiredTranslationKey);
                }
            }

            if (CollectionUtils.isNotEmpty(notFoundTranslationKeys)) {
                customMessage(context, createMessagePlaceholder("ProvidedAllLanguages.cannot.enable.lang",
                       System.lineSeparator() + elements(notFoundTranslationKeys).concatWithNewLines()), "translations");
                return false;
            }
        }
        return true;
    }
}

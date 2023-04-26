package pl.jalokim.crudwizard.genericapp.metamodel.translation.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Component
@RequiredArgsConstructor
public class ProvidedAllLanguagesValidator implements BaseConstraintValidator<ProvidedAllLanguages, Map<String, String>> {

    private final MetaModelContextService metaModelContextService;

    @Override
    public boolean isValidValue(Map<String, String> providedTranslations, ConstraintValidatorContext context) {
        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        List<String> allExpectedCountryCodes = metaModelContext.getAllCountryCodes();
        Set<String> countriesCodesForTranslations = providedTranslations.keySet();

        boolean isValid = true;
        for (String expectedCountryCode : allExpectedCountryCodes) {
            if (!countriesCodesForTranslations.contains(expectedCountryCode)) {
                isValid = false;
                customMessage(context, createMessagePlaceholder("ProvidedAllLanguages.cannot.find.translation.for.lang", expectedCountryCode));
            }
        }

        for (String countryCodeForTranslation : countriesCodesForTranslations) {
            if (!allExpectedCountryCodes.contains(countryCodeForTranslation)) {
                isValid = false;
                customMessage(context, createMessagePlaceholder("ProvidedAllLanguages.language.not.supported", countryCodeForTranslation));
            }
            if (providedTranslations.get(countryCodeForTranslation) == null) {
                isValid = false;
                customMessage(context, createMessagePlaceholder("ProvidedAllLanguages.cannot.find.translation.for.lang", countryCodeForTranslation));
            }
        }

        return isValid;
    }
}

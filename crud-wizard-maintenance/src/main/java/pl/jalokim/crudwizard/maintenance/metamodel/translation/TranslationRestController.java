package pl.jalokim.crudwizard.maintenance.metamodel.translation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.core.translations.LocaleUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.LanguageTranslationsDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationAndSourceDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationLanguageService;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationService;

@RestController
@RequestMapping("/maintenance/translations")
@RequiredArgsConstructor
@Api(tags = "translation-management")
public class TranslationRestController {

    private final TranslationLanguageService translationLanguageService;
    private final TranslationService translationService;

    @GetMapping("/by-locale/{locale}")
    @ApiOperation("Get all translations for given locale")
    public List<TranslationAndSourceDto> getTranslationsAndSourceByLocale(@PathVariable String locale) {
        return translationService.getTranslationsAndSourceByLocale(LocaleUtils.createLocale(locale));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("save or update translations for given locale")
    public void saveOrUpdateTranslations(@RequestBody LanguageTranslationsDto languageTranslationsDto) {
        translationLanguageService.saveOrUpdate(languageTranslationsDto);
    }
}

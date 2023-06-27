package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pl.jalokim.crudwizard.core.translations.MessageSourceProvider;
import pl.jalokim.crudwizard.core.translations.RefreshableMessageSource;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;

@MetamodelService
public class TranslationService extends BaseService<TranslationEntity, TranslationRepository> {

    private final List<MessageSourceProvider> messageSourceProviders;
    private final TranslationLanguageService translationLanguageService;

    public TranslationService(TranslationRepository repository,
        TranslationLanguageService translationLanguageService,
        List<MessageSourceProvider> messageSourceProviders) {
        super(repository);
        this.translationLanguageService = translationLanguageService;
        this.messageSourceProviders = messageSourceProviders;
    }

    @Override
    public TranslationEntity save(TranslationEntity translationEntity) {
        for (TranslationEntryEntity translation : translationEntity.getTranslations()) {
            translation.setTranslation(translationEntity);
            translation.setLanguage(translationLanguageService.saveNewOrLoadById(translation.getLanguage()));
        }
        return translationEntity;
    }

    public List<TranslationAndSourceDto> getTranslationsAndSourceByLocale(Locale locale) {
        Map<String, TranslationAndSourceDto> translationsByPropertyKey = new HashMap<>();
        var reversed = elements(messageSourceProviders).reversed().asImmutableList();
        for (MessageSourceProvider messageSourceProvider : reversed) {
            RefreshableMessageSource messageSource = messageSourceProvider.getMessageSource();
            Map<String, String> translations = messageSource.loadAndGetAllTranslations(List.of(locale)).get(locale);
            if (translations != null) {
                for (String translationPropertyKey : translations.keySet()) {
                    translationsByPropertyKey.put(translationPropertyKey,
                        TranslationAndSourceDto.builder()
                            .source(messageSource.getTranslationSource())
                            .translationKey(translationPropertyKey)
                            .translationValue(translations.get(translationPropertyKey))
                            .build());
                }
            }
        }
        return elements(translationsByPropertyKey.values())
            .sorted(Comparator.comparing(TranslationAndSourceDto::getTranslationKey))
            .asImmutableList();
    }
}

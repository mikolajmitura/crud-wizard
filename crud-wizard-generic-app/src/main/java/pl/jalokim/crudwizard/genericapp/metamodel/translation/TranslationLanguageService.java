package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static pl.jalokim.crudwizard.core.datetime.TimeProviderHolder.getTimeProvider;
import static pl.jalokim.crudwizard.core.translations.LocaleHolder.DEFAULT_LANG_CODE;
import static pl.jalokim.crudwizard.core.translations.LocaleUtils.createLocale;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.AbstractBaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEvent;
import pl.jalokim.crudwizard.genericapp.translation.LanguagesContext;

@MetamodelService
public class TranslationLanguageService extends
    AbstractBaseService<TranslationLanguageEntity, TranslationLanguageRepository, String> {

    private final TranslationRepository translationRepository;
    private final TranslationLanguageRepository translationLanguageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TranslationLanguageService(TranslationLanguageRepository repository, EntityManager entityManager,
        TranslationRepository translationRepository, TranslationLanguageRepository translationLanguageRepository,
        ApplicationEventPublisher applicationEventPublisher) {

        super(repository, entityManager);
        this.translationRepository = translationRepository;
        this.translationLanguageRepository = translationLanguageRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public Map<Locale, Map<String, String>> getAllTranslationsByLocales() {
        List<TranslationEntity> allTranslations = translationRepository.findAll();
        Map<Locale, Map<String, String>> allPropertiesByLocale = new HashMap<>();
        for (TranslationEntity translation : allTranslations) {
            String translationKey = translation.getTranslationKey();
            for (TranslationEntryEntity translationTranslation : translation.getTranslations()) {
                String translationValue = translationTranslation.getValueOrPlaceholder();
                String languageCode = translationTranslation.getLanguage().getLanguageCode();
                Locale locale = createLocale(languageCode);
                Map<String, String> translations = allPropertiesByLocale.computeIfAbsent(locale, locale1 -> new HashMap<>());
                translations.put(translationKey, translationValue);
            }
        }
        return allPropertiesByLocale;
    }

    public LanguagesContext createNewTranslationsContext() {
        Map<String, String> allLanguages = getAllSupportedLanguages();
        return new LanguagesContext(allLanguages);
    }

    public Map<String, String> getAllLanguages() {
        saveDefaultOneLanguage();
        return elements(repository.findAll())
            .asMap(TranslationLanguageEntity::getLanguageCode, TranslationLanguageEntity::getLanguageFullName);
    }

    public Map<String, String> getAllSupportedLanguages() {
        saveDefaultOneLanguage();
        return elements(repository.findAllByEnabledIsTrue())
            .asMap(TranslationLanguageEntity::getLanguageCode, TranslationLanguageEntity::getLanguageFullName);
    }

    @Validated
    public void saveOrUpdate(@Validated LanguageTranslationsDto languageTranslationsDto) {
        var foundOptionalLang = translationLanguageRepository.findById(languageTranslationsDto.getLanguageCode());
        TranslationLanguageEntity translationLanguageEntity;
        if (foundOptionalLang.isPresent()) {
            translationLanguageEntity = foundOptionalLang.get();
        } else {
            translationLanguageEntity = new TranslationLanguageEntity();
            translationLanguageEntity.setLanguageCode(languageTranslationsDto.getLanguageCode());
        }
        translationLanguageEntity.setEnabled(languageTranslationsDto.getEnabled());
        translationLanguageEntity.setLanguageFullName(languageTranslationsDto.getLanguageFullName());
        translationLanguageEntity = translationLanguageRepository.save(translationLanguageEntity);

        Map<String, TranslationEntity> translationEntityByKey = elements(
            translationRepository.findAllByTranslationKey(languageTranslationsDto.getTranslations().keySet()))
            .asMap(TranslationEntity::getTranslationKey);

        for (String translationKey : languageTranslationsDto.getTranslations().keySet()) {
            TranslationEntity translationEntity = translationEntityByKey.get(translationKey);
            if (translationEntity == null) {
                translationEntity = new TranslationEntity();
                translationEntity.setTranslationKey(translationKey);
                translationEntity = translationRepository.save(translationEntity);
                List<TranslationEntryEntity> translations = new ArrayList<>();
                translationEntity.setTranslations(translations);
            }

            translationEntity.getTranslations().add(TranslationEntryEntity.builder()
                .translation(translationEntity)
                .language(translationLanguageEntity)
                .valueOrPlaceholder(languageTranslationsDto.getTranslations().get(translationKey))
                .build());
        }
        if (languageTranslationsDto.getEnabled()) {
            applicationEventPublisher.publishEvent(
                new MetaModelContextRefreshEvent("after enable new language", getTimeProvider().getCurrentOffsetDateTime()));
        }
    }

    private void saveDefaultOneLanguage() {
        List<TranslationLanguageEntity> allLanguages = repository.findAll();
        if (isEmpty(allLanguages)) {
            TranslationLanguageEntity defaultLanguage = TranslationLanguageEntity.builder()
                .languageCode(DEFAULT_LANG_CODE)
                .languageFullName("English")
                .enabled(true)
                .build();
            repository.save(defaultLanguage);
        }
    }
}

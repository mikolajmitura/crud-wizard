package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;

@Mapper(config = MapperAsSpringBeanConfig.class)
public interface TranslationMapper {

    @Mapping(target = "translationId", source = "id")
    @Mapping(target = "translationByCountryCode", source = "translations")
    TranslationDto mapTranslationDto(TranslationEntity translationEntity);

    @Mapping(target = "id", source = "translationId")
    @Mapping(target = "translations", source = "translationByCountryCode")
    TranslationEntity mapTranslationEntity(TranslationDto translationEntryDto);

    @Mapping(target = "translationId", source = "id")
    @Mapping(target = "translationByCountryCode", source = "translations")
    TranslationModel mapToTranslationModel(TranslationEntity translationEntity);

    default Map<String, String> mapTranslationByCountryCode(List<TranslationEntryEntity> translations) {
        return elements(translations)
            .asMap(translation -> translation.getLanguage().getLanguageCode(), TranslationEntryEntity::getValueOrPlaceholder);
    }

    default List<TranslationEntryEntity> mapTranslations(Map<String, String> translationByCountryCode) {
        return elements(translationByCountryCode.entrySet())
            .map(entry ->
                TranslationEntryEntity.builder()
                    .valueOrPlaceholder(entry.getValue())
                    .language(TranslationLanguageEntity.builder()
                        .languageCode(entry.getKey())
                        .build())
                    .build())
            .asList();
    }
}

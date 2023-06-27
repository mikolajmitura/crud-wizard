package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Optional;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDto;

public class BeforeClassValidationUpdater {

    public static void attachFieldTranslationsWhenNotExist(ClassMetaModelDto nullableClassMetaModelDto) {

        Optional.ofNullable(nullableClassMetaModelDto)
            .ifPresent(classMetaModelDto -> {
                String classOrMetaModelName = isBlank(classMetaModelDto.getClassName()) ?
                    classMetaModelDto.getName() : classMetaModelDto.getClassName();

                TranslationDto classTranslationName = classMetaModelDto.getTranslationName();
                if (canAddTranslation(classTranslationName)) {

                    if (isNoneBlank(classOrMetaModelName)) {
                        classTranslationName.setTranslationKey("classMetaModel." + classOrMetaModelName);
                    }
                }

                Optional.ofNullable(classMetaModelDto.getEnumMetaModel())
                    .ifPresent(enumMetaModelDto ->
                        elements(enumMetaModelDto.getEnums())
                            .forEach(enumEntry -> {
                                TranslationDto enumEntryTranslation = enumEntry.getTranslation();
                                if (canAddTranslation(enumEntryTranslation)) {
                                    enumEntryTranslation.setTranslationKey("enum." + classOrMetaModelName + "." + enumEntry.getName());
                                }
                            })
                    );

                elements(classMetaModelDto.getGenericTypes())
                    .forEach(BeforeClassValidationUpdater::attachFieldTranslationsWhenNotExist);

                elements(classMetaModelDto.getExtendsFromModels())
                    .forEach(BeforeClassValidationUpdater::attachFieldTranslationsWhenNotExist);

                elements(classMetaModelDto.getFields())
                    .forEach(field -> {
                        TranslationDto translationName = field.getTranslationFieldName();
                        if (canAddTranslation(translationName)) {
                            
                            if (isNoneBlank(classOrMetaModelName)) {
                                translationName.setTranslationKey("field." + classOrMetaModelName + "." + field.getFieldName());
                            }
                        }
                        attachFieldTranslationsWhenNotExist(field.getFieldType());
                    });
            });
    }

    private static boolean canAddTranslation(TranslationDto translationName) {
        return translationName != null && isBlank(translationName.getTranslationKey());
    }
}

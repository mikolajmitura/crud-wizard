package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.isExistThatClass;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType.DEFINITION;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath.PropertyPathBuilder;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.utils.collection.CollectionUtils;

@Component
@RequiredArgsConstructor
public class OnlyExpectedFieldsForRealClassValidator implements BaseConstraintValidator<OnlyExpectedFieldsForRealClass, ClassMetaModelDto> {

    private static final Set<Class<?>> EXCLUDED_CLASSES_FOR_TRANSLATIONS_FIELDS = Set.of(Page.class, List.class, Set.class, Map.class);
    private final FieldMetaModelService fieldMetaModelService;

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModelDto, ConstraintValidatorContext context) {
        AtomicBoolean isValid = new AtomicBoolean(true);
        if (DEFINITION.equals(classMetaModelDto.getClassMetaModelDtoType()) &&
            isExistThatClass(classMetaModelDto.getClassName()) &&
            !EXCLUDED_CLASSES_FOR_TRANSLATIONS_FIELDS.contains(loadRealClass(classMetaModelDto.getClassName()))) {
            var allExpectedFields = fieldMetaModelService
                .getAllFieldsForRealClass(loadRealClass(classMetaModelDto.getClassName()));
            validateFieldsCorrectness(context, classMetaModelDto, new ArrayList<>(allExpectedFields),
                isValid, PropertyPath.builder().addNextProperty("fields"));
        }
        return isValid.get();
    }

    private void validateFieldsCorrectness(ConstraintValidatorContext context, ClassMetaModelDto classForCheck,
        List<FieldMetaModelDto> allExpectedFields, AtomicBoolean isValid, PropertyPathBuilder propertyPathBuilder) {

        if (classForCheck != null && isExistThatClass(classForCheck.getClassName())) {
            var allProvidedFields = elements(classForCheck.getFields()).asList();
            var allProvidedFieldsMap = elements(allProvidedFields)
                .asMapGroupedBy(FieldMetaModelDto::getFieldName);

            for (FieldMetaModelDto expectedField : allExpectedFields) {
                String expectedFieldName = expectedField.getFieldName();
                var fieldMetaModels = allProvidedFieldsMap.get(expectedFieldName);
                if (fieldMetaModels != null && fieldMetaModels.size() > 1) {
                    customMessage(context, createMessagePlaceholder("OnlyExpectedFieldsForRealClass.duplication.found",
                        expectedFieldName), propertyPathBuilder.build());
                    isValid.set(false);
                } else {
                    FieldMetaModelDto foundProvidedField = CollectionUtils.getFirstOrNull(elements(fieldMetaModels).asList());
                    if (foundProvidedField == null) {
                        customMessage(context, createMessagePlaceholder("OnlyExpectedFieldsForRealClass.expected.field.not.found",
                            expectedFieldName), propertyPathBuilder.build());
                        isValid.set(false);
                    }
                }
            }
        }
    }
}

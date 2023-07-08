package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.isExistThatClass;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.isNotBlank;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumEntryMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.TypeNameWrapper;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;

public class ForRealClassFieldsCanBeMergedValidator implements BaseConstraintValidator<ForRealClassFieldsCanBeMerged, ClassMetaModelDto> {

    private static final String FIELD_TYPE = "fieldType";
    public static final String FIELDS = "fields";

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModelDto, ConstraintValidatorContext context) {
        if (isExistThatClass(classMetaModelDto.getClassName()) && isNotEmpty(classMetaModelDto.getFields())) {
            ClassMetaModel classMetaModel = createClassMetaModel(createType(classMetaModelDto));
            AtomicBoolean validationPassed = new AtomicBoolean(true);
            elements(classMetaModelDto.getFields())
                .forEachWithIndex((index, field) -> {
                    if (isNotBlank(field.getFieldName())) {
                        givenFieldIsValid(context, classMetaModel, index, field, validationPassed);
                    }
                });
            return validationPassed.get();
        }
        return true;
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    private void givenFieldIsValid(ConstraintValidatorContext context, ClassMetaModel classMetaModel, Integer index,
        FieldMetaModelDto field, AtomicBoolean validationPassed) {
        FieldMetaModel fieldByName = classMetaModel.getFieldByName(field.getFieldName());

        if (fieldByName == null) {
            validationPassed.set(false);
            customMessage(context, "{ForRealClassFieldsCanBeMerged.invalid.field.name}",
                PropertyPath.builder()
                    .addNextPropertyAndIndex(FIELDS, index)
                    .addNextProperty("fieldName")
                    .build());
        } else {
            ClassMetaModel foundFieldType = fieldByName.getFieldType();
            ClassMetaModelDto fieldType = field.getFieldType();
            if (foundFieldType != null && fieldType != null &&
                !Objects.equals(foundFieldType.getClassName(), fieldType.getClassName())) {
                validationPassed.set(false);
                customMessage(context, createMessagePlaceholder("ForRealClassFieldsCanBeMerged.invalid.field.type",
                    foundFieldType.getTypeDescription()),
                    PropertyPath.builder()
                        .addNextPropertyAndIndex(FIELDS, index)
                        .addNextProperty(FIELD_TYPE)
                        .build());
            }

            if (foundFieldType != null && foundFieldType.isRealClassEnum() && field.getFieldType() != null) {
                if (field.getFieldType().getEnumMetaModel() == null) {
                    validationPassed.set(false);
                    customMessage(context, createMessagePlaceholder("ForRealClassFieldsCanBeMerged.expected.enum.translations"),
                        PropertyPath.builder()
                            .addNextPropertyAndIndex(FIELDS, index)
                            .addNextProperty(FIELD_TYPE)
                            .addNextProperty("enumMetaModel")
                            .build());
                } else {
                    var expectedEnums = elements(foundFieldType.getRealClass().getEnumConstants())
                        .map(enumEntry -> (Enum<?>) enumEntry)
                        .map(Enum::name)
                        .asList();

                    var providedFieldType = field.getFieldType();
                    var providedEnumsByName = elements(providedFieldType.getEnumMetaModel().getEnums())
                        .filter(Objects::nonNull)
                        .filter(enumEntry -> enumEntry.getName() != null)
                        .asMap(EnumEntryMetaModelDto::getName);

                    for (String expectedEnum : expectedEnums) {
                        var providedEnum = providedEnumsByName.get(expectedEnum);
                        if (providedEnum == null) {
                            validationPassed.set(false);
                            customMessage(context, createMessagePlaceholder("ForRealClassFieldsCanBeMerged.lack.enum.translation", expectedEnum),
                                PropertyPath.builder()
                                    .addNextPropertyAndIndex(FIELDS, index)
                                    .addNextProperty(FIELD_TYPE)
                                    .addNextProperty("enumMetaModel")
                                    .addNextProperty("enums")
                                    .build());
                        }
                    }

                    elements(providedFieldType.getEnumMetaModel().getEnums())
                        .forEachWithIndex((enumIndex, providedEnum) -> {
                            if (!expectedEnums.contains(providedEnum.getName())) {
                                validationPassed.set(false);
                                customMessage(context, createMessagePlaceholder("ForRealClassFieldsCanBeMerged.unknown.enum"),
                                    PropertyPath.builder()
                                        .addNextPropertyAndIndex(FIELDS, index)
                                        .addNextProperty(FIELD_TYPE)
                                        .addNextProperty("enumMetaModel")
                                        .addNextPropertyAndIndex("enums", enumIndex)
                                        .build());
                            }
                        });
                }
            }
        }
    }

    public static Type createType(ClassMetaModelDto classMetaModel) {
        Class<?> realOrBasedClass = ClassUtils.loadRealClass(classMetaModel.getClassName());
        List<ClassMetaModelDto> genericTypes = classMetaModel.getGenericTypes();
        if (isNotEmpty(genericTypes)) {
            Type[] parameters = elements(genericTypes)
                .map(ForRealClassFieldsCanBeMergedValidator::createType)
                .asArray(new Type[0]);
            return new TypeNameWrapper(new ParameterizedTypeImpl(realOrBasedClass, parameters));
        }
        return realOrBasedClass;
    }
}

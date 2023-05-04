package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.objectToRawJson
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.TRANSLATIONS_SAMPLE
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.sampleTranslationDto
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.sampleTranslationDtoWithKey
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType

import java.time.LocalDate
import org.springframework.data.domain.Page
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DefaultDataStorageQueryProvider
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator
import pl.jalokim.utils.reflection.MetadataReflectionUtils

class ClassMetaModelDtoSamples {

    static ClassMetaModelDto simplePersonClassMetaModel() {
        ClassMetaModelDto.builder()
            .name("simple-person")
            .translationName(sampleTranslationDto())
            .isGenericEnumType(false)
            .fields([
                createValidFieldMetaModelDto("id", Long, [], [isIdFieldType()]),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("birthDate", LocalDate)
            ])
            .build()
    }

    static ClassMetaModelDto extendedPersonClassMetaModel1() {
        def personMetaModel = simplePersonClassMetaModel().toBuilder().build()
        personMetaModel.getFields().addAll([
            createValidFieldMetaModelDto("documents", createListWithMetaModel(createDocumentClassMetaDto()))
        ])
        personMetaModel
    }

    static ClassMetaModelDto extendedPersonClassMetaModel2() {
        def personMetaModel = simplePersonClassMetaModel().toBuilder().build()
        personMetaModel.getFields().addAll([
            createValidFieldMetaModelDto("documents", createListWithMetaModel(createDocumentClassMetaDto())),
            createValidFieldMetaModelDto("document", createDocumentClassMetaDto())
        ])
        personMetaModel
    }

    static ClassMetaModelDto createDocumentClassMetaDto() {
        ClassMetaModelDto.builder()
            .name("document")
            .translationName(sampleTranslationDto())
            .validators([createValidValidatorMetaModelDto(NotNullValidator, NotNullValidator.NOT_NULL)])
            .fields([
                createValidFieldMetaModelDto("id", Long, [], [isIdFieldType()]),
                createValidFieldMetaModelDto("type", Byte),
                createValidFieldMetaModelDto("enumField", createEnumMetaModel("ENUM1", "ENUM2")),
                createValidFieldMetaModelDto("value", String, [
                    createValidValidatorMetaModelDto(NotNullValidator, NotNullValidator.NOT_NULL),
                    createValidValidatorMetaModelDto(SizeValidator, SizeValidator.VALIDATOR_KEY_SIZE, [min: 5, max: 25])
                ]),
                createValidFieldMetaModelDto("validFrom", LocalDate),
                createValidFieldMetaModelDto("validTo", LocalDate),
            ])
            .build()
    }

    static ClassMetaModelDto exampleClassMetaModelDtoWithExtension() {
        ClassMetaModelDto.builder()
            .name("person-with-2-extends")
            .translationName(sampleTranslationDto())
            .isGenericEnumType(false)
            .extendsFromModels([
                extendedPersonClassMetaModel1(), createClassMetaModelDtoFromClass(ExtendedSamplePersonDto)
            ])
            .fields([
                createValidFieldMetaModelDto("birthDate", LocalDate)
            ])
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoWithId(Long id) {
        buildClassMetaModelDtoWithId(id)
    }

    static ClassMetaModelDto createHttpQueryParamsClassMetaModelDto() {
        ClassMetaModelDto.builder()
            .name("person-queryParams")
            .translationName(sampleTranslationDto())
            .fields([
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("age", Integer, [],
                    [additionalProperty(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "GREATER_THAN")]),
                createValidFieldMetaModelDto("someNumbers", createClassMetaModelDtoFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelDtoFromClass(Integer)])
                    .build(), [], Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "IN")),
                createValidFieldMetaModelDto("someTexts", createClassMetaModelDtoFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelDtoFromClass(String)])
                    .build(), [], Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "IN"))
            ])
            .build()
    }

    static ClassMetaModelDto createValidEnumMetaModel() {
        ClassMetaModelDto.builder()
            .name("exampleEnum")
            .translationName(sampleTranslationDto())
            .isGenericEnumType(true)
            .enumMetaModel(sampleEnumMetaModel("VALUE_1", "VALUE_2"))
            .build()
    }

    private static EnumMetaModelDto sampleEnumMetaModel(String... enums) {
        EnumMetaModelDto.builder()
            .enums(enums.collect {
                sampleEntryMetaModel(it)
            })
            .build()
    }

    static EnumEntryMetaModelDto sampleEntryMetaModel(String enumValue, Map<String, String> translations = TRANSLATIONS_SAMPLE) {
        EnumEntryMetaModelDto.builder()
            .name(enumValue)
            .translation(sampleTranslationDto(translations))
            .build()
    }

    static ClassMetaModelDto createEnumMetaModel(String... enumValues) {
        ClassMetaModelDto.builder()
            .name("exampleEnum")
            .translationName(sampleTranslationDto())
            .isGenericEnumType(true)
            .enumMetaModel(sampleEnumMetaModel(enumValues))
            .build()
    }

    static ClassMetaModelDto createListWithMetaModel(ClassMetaModelDto classMetaModelDto) {
        ClassMetaModelDto.builder()
            .className(List.canonicalName)
            .genericTypes([classMetaModelDto])
            .build()
    }

    static ClassMetaModelDto createPageWithMetaModel(ClassMetaModelDto classMetaModelDto) {
        ClassMetaModelDto.builder()
            .className(Page.canonicalName)
            .genericTypes([classMetaModelDto])
            .build()
    }

    static ClassMetaModelDto createValidClassMetaModelDtoWithName() {
        ClassMetaModelDto.builder()
            .translationName(sampleTranslationDto())
            .name("someValidMetamodel")
            .fields([createValidFieldMetaModelDto()])
            .isGenericEnumType(false)
            .build()
    }

    static ClassMetaModelDto createValidClassMetaModelDtoWithClassName() {
        ClassMetaModelDto.builder()
            .className(String.canonicalName)
            .translationName(sampleTranslationDto())
            .isGenericEnumType(false)
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoFromClass(Class<?> metaModelClass) {
        def classBuilder = ClassMetaModelDto.builder()
            .className(metaModelClass.canonicalName)
            .isGenericEnumType(false)

        if (!isSimpleType(metaModelClass)) {
            classBuilder.translationName(sampleTranslationDto())
            classBuilder.fields(MetadataReflectionUtils.getAllFields(metaModelClass)
                .findAll() {
                    MetadataReflectionUtils.isNotStaticField(it)
                }
                .collect {
                    FieldMetaModelDto.builder()
                        .fieldName(it.name)
                        .fieldType(createClassMetaModelDtoFromClass(it.type))
                        .translationFieldName(sampleTranslationDto())
                        .build()
                })
        }

        classBuilder.build()
    }

    static ClassMetaModelDto createEmptyClassMetaModelDto() {
        ClassMetaModelDto.builder()
            .translationName(sampleTranslationDtoWithKey())
            .isGenericEnumType(false)
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto() {
        FieldMetaModelDto.builder()
            .fieldName("somefieldName")
            .fieldType(createValidClassMetaModelDtoWithClassName())
            .translationFieldName(sampleTranslationDto())
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, Class<?> fieldType,
        List<ValidatorMetaModelDto> validators = [],
        List<AdditionalPropertyDto> fieldAdditionalProperties = [],
        Map<String, String> translations = TRANSLATIONS_SAMPLE) {
        FieldMetaModelDto.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelDtoFromClass(fieldType))
            .validators(validators)
            .additionalProperties(fieldAdditionalProperties)
            .translationFieldName(sampleTranslationDto(translations))
            .build()
    }

    static FieldMetaModelDto createTranslatedFieldMetaModelDto(String fieldName, Class<?> fieldType, Map<String, String> translations) {
        createValidFieldMetaModelDto(fieldName, fieldType, [], [], translations)
    }

    static FieldMetaModelDto createIdFieldType(String fieldName, Class<?> fieldType) {
        createValidFieldMetaModelDto(fieldName, fieldType, [], [isIdFieldType()])
    }

    static FieldMetaModelDto createIgnoredForQueryFieldMetaModelDto(String fieldName, Class<?> fieldType) {
        createValidFieldMetaModelDto(fieldName, fieldType, [], [
            additionalPropertyRawJsonString(DefaultDataStorageQueryProvider.IGNORE_IN_QUERY_PARAM, "true")
        ])
    }

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, ClassMetaModelDto classMetaModelDto, Map<String, String> translations) {
        createValidFieldMetaModelDto(fieldName, classMetaModelDto, [], [:], translations)
    }

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, ClassMetaModelDto classMetaModelDto,
        List<ValidatorMetaModelDto> validators = [],
        Map<String, Object> additionalProperties = [:],
        Map<String, String> translations = TRANSLATIONS_SAMPLE) {
        FieldMetaModelDto.builder()
            .fieldName(fieldName)
            .fieldType(classMetaModelDto)
            .validators(validators)
            .translationFieldName(sampleTranslationDto(translations))
            .additionalProperties(additionalProperties.collect {
                AdditionalPropertyDto.builder()
                    .name(it.key)
                    .rawJson(objectToRawJson(it.value))
                    .build()
            })
            .build()
    }

    static AdditionalPropertyDto isIdFieldType() {
        AdditionalPropertyDto.builder()
            .name(FieldMetaModel.IS_ID_FIELD)
            .build()
    }

    static AdditionalPropertyDto additionalProperty(String name, Object value) {
        AdditionalPropertyDto.builder()
            .name(name)
            .rawJson(objectToRawJson(value))
            .build()
    }

    static AdditionalPropertyDto additionalPropertyRawJsonString(String name, String value) {
        AdditionalPropertyDto.builder()
            .name(name)
            .rawJson("\"$value\"")
            .valueRealClassName(String.canonicalName)
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoWithGenerics(Class<?> rawClass, Map<String,
        String> translations = TRANSLATIONS_SAMPLE, ClassMetaModelDto... genericTypes) {

        ClassMetaModelDto.builder()
            .className(rawClass.canonicalName)
            .translationName(sampleTranslationDto(translations))
            .genericTypes(genericTypes as List<ClassMetaModelDto>)
            .build()
    }
}

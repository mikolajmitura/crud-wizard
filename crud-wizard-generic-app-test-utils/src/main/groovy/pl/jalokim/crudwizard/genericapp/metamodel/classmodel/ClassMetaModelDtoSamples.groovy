package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel.ENUM_VALUES_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DefaultDataStorageQueryProvider
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator

class ClassMetaModelDtoSamples {

    static ClassMetaModelDto simplePersonClassMetaModel() {
        ClassMetaModelDto.builder()
            .name("person")
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
            createValidFieldMetaModelDto("document", createDocumentClassMetaDto().toBuilder()
                .validators([])
                .build())
        ])
        personMetaModel
    }

    static ClassMetaModelDto createDocumentClassMetaDto() {
        ClassMetaModelDto.builder()
            .name("document")
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
            .isGenericEnumType(false)
            .extendsFromModels([
                extendedPersonClassMetaModel1(), createClassMetaModelDtoFromClass(ExtendedSamplePersonDto)
            ])
            .fields([
                createValidFieldMetaModelDto("birthDate", Date)
            ])
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoWithId(Long id) {
        ClassMetaModelDto.builder()
            .id(id)
            .build()
    }

    static ClassMetaModelDto createHttpQueryParamsClassMetaModelDto() {
        ClassMetaModelDto.builder()
            .name("person-queryParams")
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
            .isGenericEnumType(true)
            .build()
            .addProperty(ENUM_VALUES_PREFIX, ["VALUE_1", "VALUE_2"] as String[])
    }

    static ClassMetaModelDto createEnumMetaModel(String... enumValues) {
        ClassMetaModelDto.builder()
            .name("exampleEnum")
            .isGenericEnumType(true)
            .build()
            .addProperty(ENUM_VALUES_PREFIX, enumValues)
    }

    static ClassMetaModelDto createListWithMetaModel(ClassMetaModelDto classMetaModelDto) {
        ClassMetaModelDto.builder()
            .className(List.canonicalName)
            .genericTypes([classMetaModelDto])
            .build()
    }

    static ClassMetaModelDto createValidClassMetaModelDtoWithName() {
        ClassMetaModelDto.builder()
            .name(randomText())
            .fields([createValidFieldMetaModelDto()])
            .isGenericEnumType(false)
            .build()
    }

    static ClassMetaModelDto createValidClassMetaModelDtoWithClassName() {
        ClassMetaModelDto.builder()
            .className(String.canonicalName)
            .isGenericEnumType(false)
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoFromClass(Class<?> metaModelClass) {
        ClassMetaModelDto.builder()
            .className(metaModelClass.canonicalName)
            .isGenericEnumType(false)
            .build()
    }

    static ClassMetaModelDto createEmptyClassMetaModelDto() {
        ClassMetaModelDto.builder()
            .isGenericEnumType(false)
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto() {
        FieldMetaModelDto.builder()
            .fieldName(randomText())
            .fieldType(createValidClassMetaModelDtoWithClassName())
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, Class<?> fieldType,
        List<ValidatorMetaModelDto> validators = [],
        List<AdditionalPropertyDto> fieldAdditionalProperties = []) {
        FieldMetaModelDto.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelDtoFromClass(fieldType))
            .validators(validators)
            .additionalProperties(fieldAdditionalProperties)
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, ClassMetaModelDto classMetaModelDto,
        List<ValidatorMetaModelDto> validators = [],
        Map<String, Object> additionalProperties = [:]) {
        FieldMetaModelDto.builder()
            .fieldName(fieldName)
            .fieldType(classMetaModelDto)
            .validators(validators)
            .additionalProperties(additionalProperties.collect {
                AdditionalPropertyDto.builder()
                    .name(it.key)
                    .valueAsObject(it.value)
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
            .valueAsObject(value)
            .build()
    }

    static AdditionalPropertyDto additionalPropertyRawJsonString(String name, String value) {
        AdditionalPropertyDto.builder()
            .name(name)
            .rawJson("\"$value\"")
            .valueRealClassName(String.canonicalName)
            .build()
    }
}

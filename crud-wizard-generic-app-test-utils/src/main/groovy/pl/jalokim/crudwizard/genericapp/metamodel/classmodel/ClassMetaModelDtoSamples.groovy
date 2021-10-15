package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel.ENUM_VALUES_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
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
                createValidFieldMetaModelDto("id", Long),
                createValidFieldMetaModelDto("name", String),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("birthDate", LocalDate),
            ])
            .build()
    }

    static ClassMetaModelDto extendedPersonClassMetaModel() {
        def personMetaModel = simplePersonClassMetaModel().toBuilder().build()
        personMetaModel.getFields().addAll([
            createValidFieldMetaModelDto("documents", createListWithMetaModel(
                ClassMetaModelDto.builder()
                    .name("document")
                    .validators([createValidValidatorMetaModelDto(NotNullValidator, NotNullValidator.NOT_NULL)])
                    .fields([
                        createValidFieldMetaModelDto("id", Long),
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
            ))
        ])
        personMetaModel
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

    private static ClassMetaModelDto createListWithMetaModel(ClassMetaModelDto classMetaModelDto) {
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

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, Class<?> fieldType, List<ValidatorMetaModelDto> validators = []) {
        FieldMetaModelDto.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelDtoFromClass(fieldType))
            .validators(validators)
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto(String fieldName, ClassMetaModelDto classMetaModelDto) {
        FieldMetaModelDto.builder()
            .fieldName(fieldName)
            .fieldType(classMetaModelDto)
            .build()
    }
}

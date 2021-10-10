package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

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
            .build()
    }

    static ClassMetaModelDto createValidClassMetaModelDtoWithClassName() {
        ClassMetaModelDto.builder()
            .className(String.canonicalName)
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoFromClass(Class<?> metaModelClass) {
        ClassMetaModelDto.builder()
            .className(metaModelClass.canonicalName)
            .build()
    }

    static ClassMetaModelDto createEmptyClassMetaModelDto() {
        ClassMetaModelDto.builder()
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

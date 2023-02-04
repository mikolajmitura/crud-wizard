package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.crudwizard.core.exception.EntityNotFoundException.EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEnumMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidEnumMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumClassMetaModel.ENUM_VALUES_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createEmptyValidatorMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.whenFieldIsInStateThenOthersShould
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.test.DataFakerHelper.randomLong
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import org.springframework.jdbc.core.JdbcTemplate
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState
import pl.jalokim.crudwizard.core.validation.javax.groups.FirstValidationPhase
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeRealClass
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class ClassMetaModelDtoValidationTest extends UnitTestSpec {

    private JdbcTemplate jdbcTemplate = Mock()

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(jdbcTemplate)

    def setup() {
        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> {
            args ->
                if (args[0].contains('count(ID)') && args[0].contains('ID = 1000')) {
                    return 1
                }
                return 0
        }
    }

    @Unroll
    def "should return expected messages for default context of ClassMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(classMetaModelDto, FirstValidationPhase)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        classMetaModelDto                                 | expectedErrors
        createValidClassMetaModelDtoWithName()            | []

        createValidEnumMetaModel()                        | []

        createEnumMetaModel()                             | [
            errorEntry("", getMessage("EnumValuesInAdditionalProperties.invalid.enumvalues.invalidSize")),
        ]

        createEnumMetaModel("VaLID_ENUM", "Invalid enum") | [
            errorEntry("", getMessage("EnumValuesInAdditionalProperties.invalid.at.index", 1)),
        ]

        createValidEnumMetaModel().toBuilder().build()
            .updateProperty(ENUM_VALUES_PREFIX, 1L)       | [
            errorEntry("", getMessage("EnumValuesInAdditionalProperties.invalid.enumvalues.class")),
        ]

        createClassMetaModelDtoWithId(100)                | [
            errorEntry("id", createMessagePlaceholder(EXCEPTION_CONCRETE_MESSAGE_PROPERTY_KEY,
                100, "class_meta_models").translateMessage())
        ]

        createClassMetaModelDtoWithId(1000)               | []

        createValidClassMetaModelDtoWithClassName()       | []

        createEmptyClassMetaModelDto()                    | [
            errorEntry("name", whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])))
        ]

        createValidClassMetaModelDtoWithClassName()
            .toBuilder()
            .name(randomText())
            .build()                                      | [
            errorEntry("name", whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, []))),
            errorEntry("className", whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NULL, [], "name", NOT_NULL, [])))
        ]

        createValidClassMetaModelDtoWithClassName()
            .toBuilder()
            .fields([FieldMetaModelDto.builder().build()])
            .build()                                      | [
            errorEntry("fields[0].fieldName", notNullMessage()),
            errorEntry("fields[0].fieldType", notNullMessage())
        ]

        createValidClassMetaModelDtoWithName().toBuilder()
            .genericTypes([createEmptyClassMetaModelDto().toBuilder()
                               .isGenericEnumType(null)
                               .build()])
            .validators([createEmptyValidatorMetaModelDto()])
            .build()                                      | [
            errorEntry("genericTypes", whenFieldIsInStateThenOthersShould(
                "classMetaModelDtoType", ExpectedFieldState.EQUAL_TO_ANY, ["DEFINITION"],
                fieldShouldWhenOtherMessage(NULL, [], "name", NOT_NULL, []))),
            errorEntry("genericTypes[0].isGenericEnumType", notNullMessage()),
            errorEntry("genericTypes[0].name",
                whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))),
            errorEntry("validators[0].className", notNullMessage())
        ]

        createValidClassMetaModelDtoWithClassName().toBuilder()
            .extendsFromModels([createEmptyClassMetaModelDto()])
            .validators([createValidValidatorMetaModelDto()])
            .build()                                      | [
            errorEntry("extendsFromModels", whenFieldIsInStateThenOthersShould(
                "classMetaModelDtoType", ExpectedFieldState.EQUAL_TO_ANY, ["DEFINITION"],
                fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, []))),
            errorEntry("extendsFromModels[0].name", whenFieldIsInStateThenOthersShould(
                "id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])))
        ]

        createClassMetaModelDtoFromClass(SomeRealClass).toBuilder()
            .fields([
                createValidFieldMetaModelDto("id", Long),
                createValidFieldMetaModelDto("name", String),
            ])
            .build()                                      | []

        createClassMetaModelDtoFromClass(SomeRealClass).toBuilder()
            .fields([
                createValidFieldMetaModelDto("id", Long),
                createValidFieldMetaModelDto("surname", String),
                createValidFieldMetaModelDto("name", UUID),
            ])
            .build()                                      | [
            errorEntry("fields[1].fieldName", getMessage("ForRealClassFieldsCanBeMerged.invalid.field.name")),
            errorEntry("fields[2].fieldType", getMessage("ForRealClassFieldsCanBeMerged.invalid.field.type", String.canonicalName)),
        ]
    }

    @Unroll
    def "should return expected messages for update context of ClassMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(classMetaModelDto, UpdateContext)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        classMetaModelDto                      | expectedErrors
        createValidClassMetaModelDtoWithName()
            .toBuilder()
            .id(randomLong())
            .build()                           | []
        createValidClassMetaModelDtoWithName() | [errorEntry("id", notNullMessage())]
    }
}

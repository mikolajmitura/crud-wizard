package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createEmptyValidatorMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.utils.test.DataFakerHelper.randomLong
import static pl.jalokim.utils.test.DataFakerHelper.randomText
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class ClassMetaModelDtoValidationTest extends UnitTestSpec {

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter()

    @Unroll
    def "should return expected messages for default context of ClassMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(classMetaModelDto)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        classMetaModelDto                           | expectedErrors
        createValidClassMetaModelDtoWithName()      | []
        createValidClassMetaModelDtoWithClassName() | []
        createEmptyClassMetaModelDto()              | [errorEntry("name", fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))]
        createValidClassMetaModelDtoWithClassName()
            .toBuilder()
            .name(randomText())
            .build()                                | [
            errorEntry("name", fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, [])),
            errorEntry("className", fieldShouldWhenOtherMessage(NULL, [], "name", NOT_NULL, [])),
            errorEntry("fields", fieldShouldWhenOtherMessage(NOT_EMPTY, [], "name", NOT_NULL, []))
        ]
        createValidClassMetaModelDtoWithClassName()
            .toBuilder()
            .fields([FieldMetaModelDto.builder().build()])
            .build()                                | [
            errorEntry("fields", fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, [])),
            errorEntry("fields[0].fieldName", notNullMessage()),
            errorEntry("fields[0].fieldType", notNullMessage())
        ]
        createValidClassMetaModelDtoWithName().toBuilder()
            .genericTypes([createEmptyClassMetaModelDto()])
            .validators([createEmptyValidatorMetaModelDto()])
            .build()                                | [
            errorEntry("genericTypes", fieldShouldWhenOtherMessage(NULL, [], "name", NOT_NULL, [])),
            errorEntry("genericTypes[0].name", fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])),
            errorEntry("validators[0].className", notNullMessage())
        ]
        createValidClassMetaModelDtoWithClassName().toBuilder()
            .extendsFromModels([createEmptyClassMetaModelDto()])
            .validators([createValidValidatorMetaModelDto()])
            .build()                                | [
            errorEntry("extendsFromModels", fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, [])),
            errorEntry("extendsFromModels[0].name", fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))
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

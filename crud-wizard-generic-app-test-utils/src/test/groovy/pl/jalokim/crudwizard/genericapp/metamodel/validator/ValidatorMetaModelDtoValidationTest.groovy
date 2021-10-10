package pl.jalokim.crudwizard.genericapp.metamodel.validator

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator
import pl.jalokim.crudwizard.genericapp.metamodel.validation.javax.ClassExists
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class ValidatorMetaModelDtoValidationTest extends UnitTestSpec {

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter()

    @Unroll
    def "should return expected messages for validator meta model dto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(validatorMetaModelDtoSamples)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        validatorMetaModelDtoSamples       | expectedErrors
        createValidValidatorMetaModelDto() | []

        createValidValidatorMetaModelDto().toBuilder()
            .className("not.exist")
            .build()                       | [
            errorEntry("className", messageForValidator(ClassExists, "typeOfClass", DataValidator.canonicalName))
        ]

        createValidValidatorMetaModelDto().toBuilder()
            .className(NotNullValidator.canonicalName)
            .build()                       | []

        createValidValidatorMetaModelDto().toBuilder()
            .className(BaseConstraintValidator.canonicalName)
            .build()                       | [
            errorEntry("className", messageForValidator(ClassExists, "typeOfClass", DataValidator.canonicalName))
        ]
    }
}

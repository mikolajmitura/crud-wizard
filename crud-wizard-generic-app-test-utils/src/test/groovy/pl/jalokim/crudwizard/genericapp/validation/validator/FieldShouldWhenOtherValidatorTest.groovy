package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.errorsFromViolationException

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class FieldShouldWhenOtherValidatorTest extends UnitTestSpec {

    private static exampleValidatorArgs = [
        field    : "documentId",
        should   : ExpectedFieldState.NOT_NULL,
        whenField: "passportId",
        is       : ExpectedFieldState.NULL
    ]
    private FieldShouldWhenOtherValidator testedValidator = new FieldShouldWhenOtherValidator()

    @Unroll
    def "returns expected validation result"() {
        def metaModel = ClassMetaModel.builder().name("personalDocumentData").build()
        given:
        def validationSession = createValidationSessionContext(testedValidator, metaModel, validatorArgs)

        when:
        def isValid = testedValidator.isValid(value, validationSession)

        then:
        expectedResult == isValid

        if (!isValid) {
            def foundErrors = errorsFromViolationException(validationSession.getCurrentValidatorContext().getCurrentEntries())
            assertValidationResults(foundErrors, expectedErrors)
        }

        where:
        validatorArgs        | value                                      | expectedResult | expectedErrors
        exampleValidatorArgs | [documentId: null, passportId: null]       | false          |
            [errorEntry("documentId", message("expected.message"), buildMessageForValidator(FieldShouldWhenOther))]

        exampleValidatorArgs | [documentId: "exists", passportId: null]   | true           | []
        exampleValidatorArgs | [documentId: null, passportId: "provided"] | true           | []
    }

    @Unroll
    def "return that can validate some types"() {
        when:
        def result = testedValidator.canValidate(typeToVerify)

        then:
        result == expectedResult

        where:
        typeToVerify       | expectedResult
        Map.class          | true
        Object.class       | false
        CharSequence.class | false
    }

    private static String message(String suffixCode) {
        AppMessageSourceHolder.getAppMessageSource().getMessageWithPrefix("FieldShouldWhenOtherValidatorTest", suffixCode)
    }
}

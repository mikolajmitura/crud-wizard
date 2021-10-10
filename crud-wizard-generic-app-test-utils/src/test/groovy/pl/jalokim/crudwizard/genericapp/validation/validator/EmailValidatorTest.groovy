package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidEmailMessage

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class EmailValidatorTest extends UnitTestSpec {

    private EmailValidator emailValidator = new EmailValidator()

    @Unroll
    def "returns expected validation result"() {
        given:
        def validationSession = createValidationSessionContext(emailValidator, value)

        when:
        def result = emailValidator.isValid(value, validationSession)

        then:
        result == expectedResult
        validationSession.getValidationResult().message == null
        validationSession.getValidationResult().entries.isEmpty()

        where:
        value                | expectedResult
        null                 | true
        "text"               | false
        "test@gmail.com"     | true
        "test@domain.com"    | true
        "test@@domain.com"   | false
        "test1@domain.com"   | true
        "test_1@domain.com"  | true
        "test_22@domain.com" | true
    }

    def "return expected translated message"() {
        when:
        def emailMessage = getMessage(emailValidator.messagePlaceholder())

        then:
        emailMessage == invalidEmailMessage()
    }
}

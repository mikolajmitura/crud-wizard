package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notBlankMessage

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class NotBlankValidatorTest extends UnitTestSpec {

    private NotBlankValidator notBlankValidator = new NotBlankValidator()

    @Unroll
    def "returns expected validation result"() {
        given:
        def validationSession = createValidationSessionContext(notBlankValidator, value)

        when:
        def result = notBlankValidator.isValid(value, validationSession)

        then:
        result == expectedResult
        validationSession.getValidationResult().message == null
        validationSession.getValidationResult().entries.isEmpty()

        where:
        value     | expectedResult
        null      | false
        ""        | false
        "   "     | false
        "\t \t"   | false
        "\tasd\t" | true
    }

    def "return expected translated message"() {
        when:
        def emailMessage = getMessage(notBlankValidator.messagePlaceholder())

        then:
        emailMessage == notBlankMessage()
    }
}

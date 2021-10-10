package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext

import spock.lang.Specification
import spock.lang.Unroll

class NotNullValidatorTest extends Specification {

    private NotNullValidator notNullValidator = new NotNullValidator()

    @Unroll
    def "returns expected validation result"() {
        given:
        def validationSession = createValidationSessionContext(notNullValidator, value)

        when:
        def result = notNullValidator.isValid(value, validationSession)

        then:
        result == expectedResult
        validationSession.getValidationResult().message == null
        validationSession.getValidationResult().entries.isEmpty()

        where:
        value  | expectedResult
        null   | false
        "text" | true
    }
}

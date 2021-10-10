package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext

import spock.lang.Specification
import spock.lang.Unroll

class NullValidatorTest extends Specification {

    private NullValidator nullValidator = new NullValidator()

    @Unroll
    def "returns expected validation result"() {
        given:
        def validationSession = createValidationSessionContext(nullValidator, value)

        when:
        def result = nullValidator.isValid(value, validationSession)

        then:
        result == expectedResult
        validationSession.getValidationResult().message == null
        validationSession.getValidationResult().entries.isEmpty()

        where:
        value  | expectedResult
        null   | true
        "text" | false
    }
}

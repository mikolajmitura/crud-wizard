package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class PatternValidatorTest extends UnitTestSpec {

    private PatternValidator testedValidator = new PatternValidator()

    @Unroll
    def "returns expected validation result"() {
        given:
        def validationSession = createValidationSessionContext(testedValidator, value, validatorArgs)

        when:
        def result = testedValidator.isValid(value, validationSession)

        then:
        expectedResult == result

        where:
        validatorArgs               | value  | expectedResult
        [regexp: "(\\d)*"]          | "12"   | true
        [regexp: "(\\d)*"]          | "12d"  | false
        [regexp: "123"]             | "123f" | false
        [regexp: "123"]             | "123"  | true
        [regexp: "[a-z][0-5][a-c]"] | "x4b"  | true
        [regexp: "[a-z][0-5][a-c]"] | "x4d"  | false
        [regexp: "[a-z][0-5][a-c]"] | "z5c"  | true
        [regexp: "[a-z][0-5][a-c]"] | "z5z"  | false
    }

    @Unroll
    def "return that can validate some types"() {
        when:
        def result = testedValidator.canValidate(typeToVerify)

        then:
        result == expectedResult

        where:
        typeToVerify        | expectedResult
        String.class        | true
        StringBuilder.class | true
        CharSequence.class  | true
    }
}

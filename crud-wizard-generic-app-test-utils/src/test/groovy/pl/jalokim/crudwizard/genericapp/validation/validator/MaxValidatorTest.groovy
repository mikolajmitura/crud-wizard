package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class MaxValidatorTest extends UnitTestSpec {

    public static final LinkedHashMap<String, Long> DEFAULT_MAX = [
        max_value: 2L
    ]

    private MaxValidator testedValidator = new MaxValidator()

    @Unroll
    @SuppressWarnings(["UnnecessaryBigDecimalInstantiation", "UnnecessaryBigIntegerInstantiation"])
    def "returns expected validation result"() {
        given:
        def validationSession = createValidationSessionContext(testedValidator, numberValue, validatorArgs)

        when:
        def result = testedValidator.isValid(numberValue, validationSession)

        then:
        expectedResult == result

        where:
        validatorArgs    | numberValue             | expectedResult
        DEFAULT_MAX      | null                    | true
        DEFAULT_MAX      | 1L                      | true
        DEFAULT_MAX      | 3L                      | false
        DEFAULT_MAX      | new BigDecimal("1.00")  | true
        DEFAULT_MAX      | new BigDecimal("33.00") | false
        DEFAULT_MAX      | new BigInteger("12")    | false
        DEFAULT_MAX      | new BigInteger("2")     | true
        [max_value: 33L] | new BigDecimal("33.12") | false
    }

    @Unroll
    def "return that can validate some types"() {
        when:
        def result = testedValidator.canValidate(typeToVerify)

        then:
        result == expectedResult

        where:
        typeToVerify     | expectedResult
        BigInteger.class | true
        BigDecimal.class | true
        Long.class       | true
        Float.class      | true
        int.class        | false
        Double.class     | true
    }

}

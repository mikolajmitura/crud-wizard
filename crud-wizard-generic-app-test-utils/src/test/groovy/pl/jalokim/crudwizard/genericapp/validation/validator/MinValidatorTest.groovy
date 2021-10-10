package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class MinValidatorTest extends UnitTestSpec {

    public static final LinkedHashMap<String, Long> DEFAULT_MIN = [
        min_value: 2L
    ]

    private MinValidator testedValidator = new MinValidator()

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
        validatorArgs | numberValue             | expectedResult
        DEFAULT_MIN   | null                    | true
        DEFAULT_MIN   | 1L                      | false
        DEFAULT_MIN   | 3L                      | true
        DEFAULT_MIN   | new BigDecimal("1.00")  | false
        DEFAULT_MIN   | new BigDecimal("33.00") | true
        DEFAULT_MIN   | new BigInteger("12")    | true
        DEFAULT_MIN   | new BigInteger("2")     | true
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

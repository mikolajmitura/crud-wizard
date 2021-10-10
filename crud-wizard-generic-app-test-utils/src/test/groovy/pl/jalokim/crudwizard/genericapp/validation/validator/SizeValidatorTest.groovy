package pl.jalokim.crudwizard.genericapp.validation.validator

import static ValidationSessionContextSamples.createValidationSessionContext
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidSizeMessage
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class SizeValidatorTest extends UnitTestSpec {

    public static final LinkedHashMap<String, Integer> FROM_2_TO_4 = [
        min: 2,
        max: 4
    ]

    private SizeValidator sizeValidator = new SizeValidator()

    @Unroll
    @SuppressWarnings("UnusedVariable")
    def "returns expected validation result"() {
        given:
        def realValue = value.call()
        def validationSession = createValidationSessionContext(sizeValidator, realValue, validatorArgs)

        when:
        def result = sizeValidator.isValid(realValue, validationSession)

        then:
        expectedResult == result

        where:
        validatorArgs | value | expectedResult
        FROM_2_TO_4   | {->
            Object[] args = [1, 2, 3]
            return args
        }                     | true

        FROM_2_TO_4   | {->
            Object[] args = [1, 2, 3, 4, 5]
            return args
        }                     | false

        FROM_2_TO_4   | {->
            Object[] args = [1]
            return args
        }                     | false

        FROM_2_TO_4   | {-> randomText(3)
        }                     | true

        FROM_2_TO_4   | {-> randomText(1)
        }                     | false

        FROM_2_TO_4   | {-> List<String> list = [randomText(), randomText(), randomText()]
        }                     | true

        FROM_2_TO_4   | {-> List<String> list = [randomText()]
        }                     | false

        FROM_2_TO_4   | {-> String[] intArray = [randomText()]
        }                     | false

        FROM_2_TO_4   | {-> String[] intArray = [randomText(), randomText()]
        }                     | true

        [:]           | {-> String[] intArray = [randomText()]
        }                     | true

        [:]           | {-> String[] intArray = [randomText(), randomText(), randomText(), randomText(), randomText(), randomText()]
        }                     | true

        [max: 2]      | {-> String[] intArray = [randomText(), randomText()]
        }                     | true

        [max: 2]      | {-> String[] intArray = [randomText(), randomText(), randomText()]
        }                     | false

        [max: 2]      | {-> String[] intArray = []
        }                     | true

        [max: 2]      | {-> null
        }                     | true

        [max: 2]      | {-> boolean[] boolArray = []
        }                     | true

        [max: 2]      | {-> int[] rawIntArray = []
        }                     | true

        [max: 2]      | {-> int[] rawIntArray = [1, 2, 3]
        }                     | false
    }

    @Unroll
    def "return that can validate some types"() {
        when:
        def result = sizeValidator.canValidate(typeToVerify)

        then:
        result == expectedResult

        where:
        typeToVerify        | expectedResult
        String[].class      | true
        String.class        | true
        Collection.class    | true
        List.class          | true
        CharSequence.class  | true
        Double.class        | false
        DataValidator.class | false
        Object[].class      | true
        int[].class         | true
        boolean[].class     | true
        double[].class      | true
        Map[].class         | true
        Map.class           | true
    }

    def "return expected translated message"() {
        given:
        Map<String, Object> messagePlaceholders = [
            min: 2,
            max: 4
        ]

        when:
        def translatedMessage = createMessagePlaceholder(sizeValidator.messagePlaceholder(), messagePlaceholders).translateMessage()

        then:
        translatedMessage == invalidSizeMessage(2, 4)
    }
}

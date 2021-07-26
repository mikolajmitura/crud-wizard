package pl.jalokim.crudwizard.test.utils.translations

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createCommonMessageSource
import static pl.jalokim.crudwizard.core.translations.MessageSourceFactory.createMessageSource
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_BLANK_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_NULL_MESSAGE_PROPERTY
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import javax.validation.Validator
import javax.validation.constraints.DecimalMax
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import pl.jalokim.crudwizard.core.translations.ExampleEnum
import pl.jalokim.utils.test.DataFakerHelper
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Specification

class AppMessageSourceTestImplTest extends Specification {

    AppMessageSourceTestImpl testCase = new AppMessageSourceTestImpl()
    MessageSource commonsMessageSource = createCommonMessageSource()
    MessageSource appMessageSource = createMessageSource()

    private Validator validator

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter()

    def setup() {
        validator = validatorWithConverter.getValidator()
    }

    def "should find property"() {
        when:
        def propertyValue = testCase.getMessage("example.property")
        then:
        propertyValue == "example value"
    }

    def "should find property with placeholders by index"() {
        when:
        def propertyValue = testCase.getMessage("pl.jalokim.crudwizard.core.exception.handler.DummyRestController.some.code",
            ExampleEnum.TEST, "{some.placeholder}")
        then:
        propertyValue == "placeholders TEST {some.placeholder}"
    }

    def "should not find property"() {
        def notExistPropertyKey = "example.property.not.exists"
        when:
        testCase.getMessage(notExistPropertyKey)
        then:
        NoSuchMessageException ex = thrown()
        ex.message.contains("No message found under code '${notExistPropertyKey}'")
    }

    def "should find property and change placeholders"() {
        when:
        def propertyValue = testCase.getMessage("pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImplTest.placeholders",
            [anotherProperty: "{example.property}",
             rawValue       : "raw value",
             someEnum       : ExampleEnum.TEST
            ]
        )
        then:
        propertyValue == "example value {example.property} raw value test:{example.property} enum: TEST"
    }

    def "should get message from common messages"() {
        when:
        def message = testCase.getMessage(NOT_NULL_MESSAGE_PROPERTY)
        then:
        message == commonsMessageSource.getMessage(NOT_NULL_MESSAGE_PROPERTY, null, Locale.getDefault())
    }

    def "should get message from app messages"() {
        when:
        def message = testCase.getMessage(NOT_BLANK_MESSAGE_PROPERTY)
        then:
        message == appMessageSource.getMessage(NOT_BLANK_MESSAGE_PROPERTY, null, Locale.getDefault())
        message != commonsMessageSource.getMessage(NOT_BLANK_MESSAGE_PROPERTY, null, Locale.getDefault())
    }

    def "should get message for javax validation size"() {
        when:
        def result = AppMessageSourceTestImpl.invalidSizeMessage(10, 12)

        then:
        result == testCase.getMessage("javax.validation.constraints.Size.message", [min: 10, max: 12])
    }

    def "should get message for javax validation length"() {
        when:
        def result = AppMessageSourceTestImpl.invalidLengthMessage(10, 12)

        then:
        result == testCase.getMessage("org.hibernate.validator.constraints.Length.message", [min: 10, max: 12])
    }

    def "should get message for javax validation range"() {
        when:
        def result = AppMessageSourceTestImpl.invalidRangeMessage(10, 12)

        then:
        result == testCase.getMessage("org.hibernate.validator.constraints.Range.message", [min: 10, max: 12])
    }

    def "should get message for javax validation min"() {
        when:
        def result = AppMessageSourceTestImpl.invalidMinMessage(10)

        then:
        result == testCase.getMessage("javax.validation.constraints.Min.message", [value: 10])
    }

    def "should get message for javax validation max"() {
        when:
        def result = AppMessageSourceTestImpl.invalidMaxMessage(12)

        then:
        result == testCase.getMessage("javax.validation.constraints.Max.message", [value: 12])
    }

    def "should get message for javax validation not null"() {
        when:
        def result = AppMessageSourceTestImpl.notNullMessage()

        then:
        result == testCase.getMessage("javax.validation.constraints.NotNull.message")
    }

    def "should get message for javax validation not blank"() {
        when:
        def result = AppMessageSourceTestImpl.notBlankMessage()

        then:
        result == testCase.getMessage("javax.validation.constraints.NotBlank.message")
    }

    def "should get message for javax validation not empty"() {
        when:
        def result = AppMessageSourceTestImpl.notEmptyMessage()

        then:
        result == testCase.getMessage("javax.validation.constraints.NotEmpty.message")
    }

    def "should get message for javax validation invalid email"() {
        when:
        def result = AppMessageSourceTestImpl.invalidEmailMessage()

        then:
        result == testCase.getMessage("javax.validation.constraints.Email.message")
    }

    def "should get message for javax validation invalid pattern"() {
        given:
        def text = DataFakerHelper.randomText()

        when:
        def result = AppMessageSourceTestImpl.invalidPatternMessage(text)

        then:
        result == testCase.getMessage("javax.validation.constraints.Pattern.message", [regexp: text])
    }

    def "should return expected message for DecimalMax"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(new SomeBean(
            someDecimalNumber: 1000.00G,
        ))

        then:
        assertValidationResults(foundErrors, [
            errorEntry("someDecimalNumber", messageForValidator(DecimalMax, ["value": "999.88", inclusive: false]))
        ])
    }

    def "should get message for EntityNotFoundException.default.message"() {
        when:
        def result = AppMessageSourceTestImpl.entityNotFoundMessage(12000)

        then:
        result == testCase.getMessage("EntityNotFoundException.default.message", 12000)
    }

    def "should return the same translated values for interpolation by map and raw args"() {
        given:
        Long arg1 = 12000
        Long arg2 = 13000

        when:
        def interpolatedByIndexes = testCase.getMessage("some.another.withIndexes", arg1, arg2)
        def interpolatedWithPlaceholders = testCase.getMessage("some.another.withPlaceholders", [first: arg1, second: arg2])

        then:
        interpolatedByIndexes == "some value: $arg1, $arg2"
        interpolatedByIndexes == interpolatedWithPlaceholders
    }

    static class SomeBean {

        @DecimalMax(value = "999.88", inclusive = false)
        BigDecimal someDecimalNumber
    }
}

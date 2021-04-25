package pl.jalokim.crudwizard.core.exception.handler

import pl.jalokim.crudwizard.core.exception.BusinessLogicException
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl
import spock.lang.Specification
import spock.lang.Unroll

class GlobalExceptionHandlerTest extends Specification {

    GlobalExceptionHandler testCase

    def setup() {
        testCase = new GlobalExceptionHandler()
    }

    def setupSpec() {
        AppMessageSourceTestImpl.initStaticAppMessageSource()
    }

    @Unroll
    def "should return expected #expectedErrorCode for #exception"() {
        when:
        def code = GlobalExceptionHandler.extractErrorCode(exception)
        then:
        code == expectedErrorCode
        expectedMessage == exception.getMessage()
        where:
        expectedMessage         | expectedErrorCode               || exception
        "some message"          | null                            || new BusinessLogicException("some message")
        "example value"         | "example.property"              || new BusinessLogicException("{example.property}")
        "Some global exception" | "some.global.exception.message" || new EntityNotFoundException(MessagePlaceholder.builder()
            .mainPlaceholder("some.global.exception.message")
            .build())
        "Some global exception" | "fixed.error.code"              || new EntityNotFoundException(MessagePlaceholder.builder()
            .mainPlaceholder("some.global.exception.message")
            .errorCode("fixed.error.code")
            .build())
    }
}

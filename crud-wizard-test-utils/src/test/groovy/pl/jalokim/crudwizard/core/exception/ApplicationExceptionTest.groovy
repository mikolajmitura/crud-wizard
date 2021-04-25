package pl.jalokim.crudwizard.core.exception

import pl.jalokim.crudwizard.core.translations.TestAppMessageSourceHolder
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl
import spock.lang.Specification

class ApplicationExceptionTest extends Specification {

    def "should translate message when there is AppMessageSource in static holder"() {
        given:
        AppMessageSourceTestImpl.initStaticAppMessageSource()

        when:
        throwException("{example.property}")

        then:
        ApplicationException ex = thrown()
        ex.message == "example value"
    }

    def "should not translate message when there is AppMessageSource in static holder"() {
        given:
        AppMessageSourceTestImpl.initStaticAppMessageSource()

        when:
        throwException("example.property")

        then:
        ApplicationException ex = thrown()
        ex.message == "example.property"
    }

    def "should note translate message when there is no AppMessageSource in static holder"() {
        given:
        TestAppMessageSourceHolder.setAppMessageSource(null)

        when:
        throwException("{example.property}")

        then:
        ApplicationException ex = thrown()
        ex.message == "{example.property}"
    }

    private static void throwException(String message) {
        throw new BusinessLogicException(message)
    }
}

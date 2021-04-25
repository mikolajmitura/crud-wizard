package pl.jalokim.crudwizard.test.utils.translations

import org.springframework.context.NoSuchMessageException
import spock.lang.Specification

class AppMessageSourceTestImplTest extends Specification {

    AppMessageSourceTestImpl testCase = new AppMessageSourceTestImpl()

    def "should find property"() {
        when:
        def propertyValue = testCase.getMessage("example.property")
        then:
        propertyValue == "example value"
    }

    def "should not find property"() {
        def notExistPropertyKey = "example.property.not.exists"
        when:
        testCase.getMessage(notExistPropertyKey)
        then:
        NoSuchMessageException ex = thrown()
        ex.message.contains("No message found under code '${notExistPropertyKey}'")
    }
}

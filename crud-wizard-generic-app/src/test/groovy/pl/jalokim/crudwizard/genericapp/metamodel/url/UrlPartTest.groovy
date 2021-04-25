package pl.jalokim.crudwizard.genericapp.metamodel.url

import spock.lang.Specification
import spock.lang.Unroll

class UrlPartTest extends Specification {

    @Unroll
    def "path part is variable path: #expectedValue"() {
        when:
        def result = urlPart.isPathVariable()

        then:
        result == expectedValue

        where:
        expectedValue || urlPart
        true          || variableUrlPart("text")
        false         || normalUrlPart("text")
    }

    static UrlPart variableUrlPart(String variableName) {
        new UrlPart("{${variableName}}", variableName)
    }

    static UrlPart normalUrlPart(String normalPathPart) {
        new UrlPart(normalPathPart, null)
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.url

import static pl.jalokim.crudwizard.core.metamodels.url.UrlPart.normalUrlPart
import static pl.jalokim.crudwizard.core.metamodels.url.UrlPart.variableUrlPart

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
}

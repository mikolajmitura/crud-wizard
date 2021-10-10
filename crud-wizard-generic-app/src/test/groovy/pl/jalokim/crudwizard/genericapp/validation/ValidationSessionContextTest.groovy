package pl.jalokim.crudwizard.genericapp.validation

import spock.lang.Specification
import spock.lang.Unroll

class ValidationSessionContextTest extends Specification {

    @Unroll
    def "return expected concatenated paths"() {
        when:
        def result = ValidationSessionContext.concatPaths(path1, path2)
        then:
        expectedResult == result

        where:
        path1     | path2      | expectedResult
        ""        | ""         | ""
        ""        | "name"     | "name"
        "person"  | "name"     | "person.name"
        ""        | "[0]"      | "[0]"
        ""        | "[0].node" | "[0].node"
        "persons" | "[0]"      | "persons[0]"
        "person"  | ""         | "person"
    }
}

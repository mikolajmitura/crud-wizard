package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata

import spock.lang.Specification
import spock.lang.Unroll

class MethodCodeMetadataTest extends Specification {

    @Unroll
    def "regenerate method name with"() {
        when:
        def result = MethodCodeMetadata.regenerateMethodName(currentMethodName, currentMetodNames)

        then:
        result == expectedMethodName

        where:
        currentMethodName | expectedMethodName | currentMetodNames
        "method"          | "method_1"         | ["method", "other_method_name"] as Set
        "method"          | "method"           | ["other_method_name"] as Set
        "method"          | "method_2"         | ["method_1", "method", "other_method_name"] as Set
        "method"          | "method_12"        | ["method_1", "method_11", "method", "other_method_name"] as Set
        "method"          | "method_100"       | ["method_1", "method_11", "method_99", "method", "other_method_name"] as Set
    }
}

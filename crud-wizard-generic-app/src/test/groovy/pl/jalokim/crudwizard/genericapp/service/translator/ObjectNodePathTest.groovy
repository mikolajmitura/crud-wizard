package pl.jalokim.crudwizard.genericapp.service.translator

import spock.lang.Specification
import spock.lang.Unroll

class ObjectNodePathTest extends Specification {

    @Unroll
    def "should return expected full path"() {
        when:
        def result = inputObjectNodePath.fullPath

        then:
        result == expectedPath

        where:
        inputObjectNodePath     | expectedPath
        ObjectNodePath.rootNode()
            .nextNode("addresses")
            .nextCollectionNode(1)
            .nextNode("street") | "addresses[1].street"

        ObjectNodePath.rootNode()
            .nextCollectionNode(12)
            .nextCollectionNode(1)
            .nextNode("address")
            .nextNode("houses")
            .nextCollectionNode(2)
            .nextCollectionNode(3)
            .nextNode("last")   | "[12][1].address.houses[2][3].last"
    }
}

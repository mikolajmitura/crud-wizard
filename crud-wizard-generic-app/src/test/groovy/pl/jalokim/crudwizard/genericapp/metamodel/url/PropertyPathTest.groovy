package pl.jalokim.crudwizard.genericapp.metamodel.url

import spock.lang.Specification
import spock.lang.Unroll

class PropertyPathTest extends Specification {

    @Unroll
    def "return expected full path"() {
        when:
        def fullPath = propertyPath.buildFullPath()

        then:
        expectedFullPath == fullPath

        where:
        propertyPath                   | expectedFullPath
        PropertyPath.createRoot()
            .nextWithIndex(1)
            .nextWithName("map")
            .nextWithName("someField") | "[1].map.someField"

        PropertyPath.createRoot()
            .nextWithName("map")
            .nextWithName("someField")
            .nextWithIndex(2)          | "map.someField[2]"

        PropertyPath.createRoot()      | ""

        PropertyPath.createRoot()
            .nextWithIndex(3)          | "[3]"

        PropertyPath.createRoot()
            .nextWithName("field")     | "field"
    }

    @Unroll
    def "concate paths as expected"() {
        when:
        def result = currentPath.concat(pathToConcate)

        then:
        result == expectedPath
        result.buildFullPath() == expectedFullText

        where:
        currentPath | pathToConcate             | expectedPath   | expectedFullText
        PATH1       | PATH1_TC                  | PATH1_EXPECTED | "[1].map.someField[*][12].test"
        PATH2       | PATH2_TC                  | PATH2_EXPECTED | "map[1][2].someField[2].test[*][*]"
        PATH1       | PropertyPath.createRoot() | PATH1          | "[1].map.someField"
    }

    private static final PropertyPath PATH1 = PropertyPath.createRoot().nextWithIndex(1)
        .nextWithName("map").nextWithName("someField")

    private static final PropertyPath PATH1_TC = PropertyPath.createRoot()
        .nextWithAllIndexes().nextWithIndex(12).nextWithName("test")

    private static final PropertyPath PATH1_EXPECTED = PropertyPath.createRoot().nextWithIndex(1).nextWithName("map")
        .nextWithName("someField").nextWithAllIndexes().nextWithIndex(12).nextWithName("test")

    private static final PropertyPath PATH2 = PropertyPath.createRoot().nextWithName("map").nextWithIndex(1)
        .nextWithIndex(2).nextWithName("someField").nextWithIndex(2)

    private static final PropertyPath PATH2_TC = PropertyPath.createRoot()
        .nextWithName("test").nextWithAllIndexes().nextWithAllIndexes()

    private static final PropertyPath PATH2_EXPECTED = PropertyPath.createRoot().nextWithName("map").nextWithIndex(1)
        .nextWithIndex(2).nextWithName("someField").nextWithIndex(2).nextWithName("test").nextWithAllIndexes().nextWithAllIndexes()
}

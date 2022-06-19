package pl.jalokim.crudwizard.genericapp.metamodel.validator

import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath
import spock.lang.Specification
import spock.lang.Unroll

class PropertyPathResolverTest extends Specification {

    public static final PropertyPath EXPECTED_PATH_1 = PropertyPath.createRoot().nextWithName("list").nextWithIndex(1)
        .nextWithName("fieldName").nextWithAllIndexes().nextWithAllIndexes().nextWithIndex(12).nextWithName("nextNode")

    @Unroll
    def "return expected property path"() {
        when:
        def result = PropertyPathResolver.resolvePath(pathAsText)

        then:
        result.buildFullPath() == pathAsText
        result == expectedPropertyPath

        where:
        pathAsText                             | expectedPropertyPath
        "list[1].fieldName[*][*][12].nextNode" | EXPECTED_PATH_1
        ""                                     | PropertyPath.createRoot()
        "name"                                 | PropertyPath.createRoot().nextWithName("name")
        "[1]"                                  | PropertyPath.createRoot().nextWithIndex(1)
    }
}

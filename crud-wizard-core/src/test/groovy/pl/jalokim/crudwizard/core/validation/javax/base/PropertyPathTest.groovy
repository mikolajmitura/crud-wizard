package pl.jalokim.crudwizard.core.validation.javax.base

import spock.lang.Specification
import spock.lang.Unroll

class PropertyPathTest extends Specification {

    @Unroll
    def "return expected path parts"() {
        when:
        def result = pathBuilder.build().asFullPath()

        then:
        result == expectedFullPath

        where:
        pathBuilder                                                            | expectedFullPath
        PropertyPath.builder().addNextIndex(1).addNextIndex(12)                | '[1][12]'
        PropertyPath.builder().addNextProperty("list").addNextIndex(12)        | 'list[12]'
        PropertyPath.builder().addNextPropertyAndIndex("list", 12)             | 'list[12]'
        PropertyPath.builder().addNextPropertyAndIndex("list", 12)
            .addNextProperty("nextField").addNextPropertyAndIndex("array", 13) | 'list[12].nextField.array[13]'
    }
}

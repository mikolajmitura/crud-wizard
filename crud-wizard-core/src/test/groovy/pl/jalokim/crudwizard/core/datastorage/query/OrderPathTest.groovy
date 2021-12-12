package pl.jalokim.crudwizard.core.datastorage.query

import static pl.jalokim.crudwizard.core.datastorage.query.OrderDirection.ASC
import static pl.jalokim.crudwizard.core.datastorage.query.OrderDirection.DESC
import static pl.jalokim.crudwizard.core.datastorage.query.OrderPath.newOrder

import spock.lang.Specification
import spock.lang.Unroll

class OrderPathTest extends Specification {

    @Unroll
    def "return expected order path"() {
        when:
        def result = OrderPath.orderPathFromText(inputPath)

        then:
        result == expected

        where:
        expected                      | inputPath
        newOrder("simple", ASC)       | "simple"
        newOrder("person.name", ASC)  | "person.name"
        newOrder("person.name", ASC)  | "person.name(asc)"
        newOrder("person.name", DESC) | "person.name(desc)"
    }


}

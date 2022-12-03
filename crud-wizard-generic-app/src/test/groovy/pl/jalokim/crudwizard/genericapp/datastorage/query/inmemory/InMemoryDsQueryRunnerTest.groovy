package pl.jalokim.crudwizard.genericapp.datastorage.query.inmemory

import static PersonDataSamples.PERSON1
import static PersonDataSamples.PERSON2
import static PersonDataSamples.PERSON3
import static PersonDataSamples.PERSON4
import static PersonDataSamples.PERSON5
import static PersonDataSamples.peopleList
import static org.springframework.data.domain.Sort.Order.asc
import static org.springframework.data.domain.Sort.Order.desc

import org.springframework.data.domain.Sort
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery
import pl.jalokim.crudwizard.genericapp.datastorage.query.EmptyExpression
import pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression
import spock.lang.Specification
import spock.lang.Unroll

class InMemoryDsQueryRunnerTest extends Specification {

    InMemoryWhereExpressionTranslator inMemoryWhereExpressionTranslator = new InMemoryWhereExpressionTranslator()
    InMemoryOrderByTranslator inMemoryOrderByTranslator = new InMemoryOrderByTranslator()
    InMemoryDsQueryRunner testCase = new InMemoryDsQueryRunner(inMemoryWhereExpressionTranslator, inMemoryOrderByTranslator)

    @Unroll
    def "return expected order and filtered list"() {
        given:
        def query = DataStorageQuery.builder()
            .where(wherePart)
            .sortBy(orderPart)
            .build()

        when:
        def results = testCase.runQuery(peopleList().stream(), query)

        then:
        results == expectedResults

        where:
        expectedResults                               | wherePart                          | orderPart
        peopleList()                                  | new EmptyExpression()              | null
        peopleList()                                  | null                               | null
        [PERSON2, PERSON3, PERSON4, PERSON5]          | RealExpression.isNotNull("father") | null
        [PERSON3, PERSON2, PERSON5, PERSON4]          | RealExpression.isNotNull("father") | Sort.by("someLong")
        [PERSON4, PERSON5, PERSON2, PERSON3]          | RealExpression.isNotNull("father") | Sort.by(desc("someLong"))
        [PERSON3, PERSON2, PERSON1, PERSON5, PERSON4] | RealExpression.isNotNull("name")   | Sort.by(asc("someLong"), desc("name"))
        [PERSON3, PERSON1, PERSON2, PERSON5, PERSON4] | RealExpression.isNotNull("name")   | Sort.by(asc("someLong"), asc("name"))
    }
}

package pl.jalokim.crudwizard.genericapp.datastorage.query.inmemory

import static PersonDataSamples.PERSON1
import static PersonDataSamples.PERSON2
import static PersonDataSamples.PERSON3
import static PersonDataSamples.PERSON4
import static PersonDataSamples.PERSON5
import static PersonDataSamples.peopleList
import static pl.jalokim.crudwizard.genericapp.datastorage.query.ExpressionArgument.buildForPath
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.greaterThan
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.isEqualsTo
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.like
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.likeIgnoreCase
import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.lowerThan

import pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings([
    "ExplicitCallToAndMethod", "ExplicitCallToOrMethod"
])
class InMemoryWhereExpressionTranslatorTest extends Specification {

    InMemoryWhereExpressionTranslator inMemoryWhereExpressionTranslator = new InMemoryWhereExpressionTranslator()

    @Unroll
    def "return expected list by given query"() {
        when:
        def filter = inMemoryWhereExpressionTranslator.translateWhereExpression(query)
        def resultList = peopleList().findAll {
            filter.test(it)
        }

        then:
        resultList == expectedList

        where:
        expectedList                | query
        [PERSON1, PERSON2]          | likeIgnoreCase("surname", "doe")
        [PERSON3, PERSON4, PERSON5] | likeIgnoreCase("surname", "doe").negate()
        [PERSON3, PERSON4, PERSON5] | (likeIgnoreCase("surname", "d").and(likeIgnoreCase("name", "j"))).negate()
        [PERSON1, PERSON2, PERSON5] | likeIgnoreCase("surname", "d").or(likeIgnoreCase("name", "j"))
        [PERSON3, PERSON4]          | (likeIgnoreCase("surname", "d").or(likeIgnoreCase("name", "j"))).negate()
        [PERSON5]                   | likeIgnoreCase(buildForPath("surname"), buildForPath("name"))
        [PERSON2, PERSON3]          | likeIgnoreCase("father.name", "john")
        []                          | likeIgnoreCase("surname", "doe").and(likeIgnoreCase("name", "Marry"))
        [PERSON1, PERSON2]          | like("name", "Jo")
        []                          | like("name", "jo")
        [PERSON3, PERSON4]          | isEqualsTo("surname", "Jane")
        [PERSON3, PERSON4]          | isEqualsTo("surname", "Jane")
        [PERSON1]                   | isEqualsTo("someDouble", new Double(0.3))
        [PERSON1]                   | lowerThan("someDouble", 0.5)
        [PERSON3]                   | lowerThan("someLong", 10)
        [PERSON4, PERSON5]          | greaterThan("someLong", 10)
        [PERSON4, PERSON5]          | RealExpression.in("someLong", [9L, 12L, 13L])
        [PERSON1]                   | RealExpression.isNull("father")
        [PERSON2, PERSON3,
         PERSON4, PERSON5]          | RealExpression.isNotNull("father")
    }

}

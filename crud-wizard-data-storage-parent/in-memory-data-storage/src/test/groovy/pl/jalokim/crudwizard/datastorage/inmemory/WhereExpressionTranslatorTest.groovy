package pl.jalokim.crudwizard.datastorage.inmemory

import static pl.jalokim.crudwizard.core.datastorage.query.ExpressionArgument.buildForPath
import static pl.jalokim.crudwizard.core.datastorage.query.RealExpression.likeIgnoreCase

import spock.lang.Specification
import spock.lang.Unroll

class WhereExpressionTranslatorTest extends Specification {

    private static Person person1 = new Person(name: "John", surname: "Doe")
    private static Person person2 = new Person(name: "Jonathan", surname: "Does", father: person1)
    private static Person person3 = new Person(name: "Marry", surname: "Jane", father: person1)
    private static Person person4 = new Person(name: "Nathan", surname: "Jane", father: person2)
    private static Person person5 = new Person(name: "Jane", surname: "Hajanesons", father: person2)

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
        [person1, person2]          | likeIgnoreCase("surname", "doe")
        [person3, person4, person5] | likeIgnoreCase("surname", "doe").negate()
        [person3, person4, person5] | (likeIgnoreCase("surname", "d").and(likeIgnoreCase("name", "j"))).negate()
        [person1, person2, person5] | likeIgnoreCase("surname", "d").or(likeIgnoreCase("name", "j"))
        [person3, person4]          | (likeIgnoreCase("surname", "d").or(likeIgnoreCase("name", "j"))).negate()
        [person5]                   | likeIgnoreCase(buildForPath("surname"), buildForPath("name"))
        [person2, person3]          | likeIgnoreCase("father.name", "john")
        []                          | likeIgnoreCase("surname", "doe").and(likeIgnoreCase("name", "Marry"))
    }

    private static List<Person> peopleList() {

        return [person1, person2, person3, person4, person5]
    }

    private static class Person {
        private String name
        private String surname
        private Person father
    }
}

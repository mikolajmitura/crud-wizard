package pl.jalokim.crudwizard.core.utils

import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath
import static pl.jalokim.utils.test.DataFakerHelper.randomLong
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import spock.lang.Specification
import spock.lang.Unroll

class ValueExtractorFromPathTest extends Specification {

    final static SOME_LONG_1 = randomLong()
    final static SOME_LONG_2 = randomLong()
    final static CORP_NAME = randomText()

    final static MOTHER = new Person(name: randomText(), surname: randomText(), additionalFields: [
        someLong2: SOME_LONG_2
    ])
    final static FATHER = new Person(name: randomText(), surname: randomText())
    final static PERSON = new Person(name: randomText(), surname: randomText(),
        mother: MOTHER, father: FATHER, additionalFields: [
        someLong1: SOME_LONG_1
    ])

    final static OWNER_CORP = new CompanyOwner(name: randomText(), surname: randomText(), companyName: CORP_NAME)

    @Unroll
    def "return expected value of field by path"() {
        given:

        def someMap = [
            someField: PERSON,
            owner    : OWNER_CORP
        ]

        when:
        def result = getValueFromPath(someMap, fullPath)

        then:
        result == expectedValue

        where:
        fullPath                                       | expectedValue
        "notExists"                                    | null
        "someField"                                    | PERSON
        "someField.name"                               | PERSON.name
        "someField.mother"                             | MOTHER
        "someField.mother.mother.father"               | null
        "someField.mother.?additionalFields.someLong2" | SOME_LONG_2
        "someField.mother.additionalFields.someLong1"  | null
        "owner.?companyName"                           | CORP_NAME
    }

    static class Person {

        String name
        String surname
        Person father
        Person mother
        Map<String, Object> additionalFields
    }

    static class CompanyOwner extends Person {

        String companyName
    }
}

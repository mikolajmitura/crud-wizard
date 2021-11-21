package pl.jalokim.crudwizard.genericapp.service

import static pl.jalokim.crudwizard.genericapp.service.ValueExtractorFromPath.getValueFromPath
import static pl.jalokim.utils.test.DataFakerHelper.randomLong
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import spock.lang.Specification
import spock.lang.Unroll

class ValueExtractorFromPathTest extends Specification {

    static def SOME_LONG_1 = randomLong()
    static def SOME_LONG_2 = randomLong()
    static def MOTHER = new Person(name: randomText(), surname: randomText(), additionalFields: [
        someLong2: SOME_LONG_2
    ])
    static def FATHER = new Person(name: randomText(), surname: randomText())
    static def PERSON = new Person(name: randomText(), surname: randomText(),
        mother: MOTHER, father: FATHER, additionalFields: [
        someLong1: SOME_LONG_1
    ])

    @Unroll
    def "return expected value of field by path"() {
        given:

        def someMap = [
            someField: PERSON
        ]

        when:
        def result = getValueFromPath(someMap, fullPath)

        then:
        result == expectedValue

        where:
        fullPath                                      | expectedValue
        "notExists"                                   | null
        "someField"                                   | PERSON
        "someField.name"                              | PERSON.name
        "someField.mother"                            | MOTHER
        "someField.mother.mother.father"              | null
        "someField.mother.additionalFields.someLong2" | SOME_LONG_2
        "someField.mother.additionalFields.someLong1" | null
    }

    static class Person {
        String name
        String surname
        Person father
        Person mother
        Map<String, Object> additionalFields
    }
}

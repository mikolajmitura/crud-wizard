package pl.jalokim.crudwizard.core.utils

import spock.lang.Specification
import spock.lang.Unroll

class StringCaseUtilsTest extends Specification {

    @Unroll
    def "return expected text with first letter as uppercase"() {
        when:
        def result = StringCaseUtils.firstLetterToUpperCase(text)

        then:
        result == expected

        where:
        text        | expected
        "fieldSome" | "FieldSome"
        "SomeValue" | "SomeValue"
    }

    @Unroll
    def "return expected text with first letter as lowercase"() {
        when:
        def result = StringCaseUtils.firstLetterToLowerCase(text)

        then:
        result == expected

        where:
        text        | expected
        "fieldSome" | "fieldSome"
        "SomeValue" | "someValue"
    }
}

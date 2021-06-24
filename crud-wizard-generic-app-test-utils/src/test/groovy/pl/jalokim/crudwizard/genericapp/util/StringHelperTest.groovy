package pl.jalokim.crudwizard.genericapp.util

import pl.jalokim.crudwizard.core.utils.StringHelper
import spock.lang.Specification

class StringHelperTest extends Specification {

    def "should clear all given texts"() {
        given:
        def textToClear = "//this is some\$ text{}"
        when:
        def result = StringHelper.replaceAllWithEmpty(textToClear, "/", '$', "{", "}")
        then:
        result == "this is some text"
    }
}

package pl.jalokim.crudwizard.genericapp.util

import pl.jalokim.crudwizard.core.utils.CustomPropertiesResolver
import spock.lang.Specification

class CustomPropertiesResolverTest extends Specification {

    def "should add element at some index"() {
        given:
        List<String> list = []

        when:
        CustomPropertiesResolver.addToCollectionAtIndex(list, 3, "test")

        then:
        list == [null, null, null, "test"]

        and:
        when:
        CustomPropertiesResolver.addToCollectionAtIndex(list, 6, "atSix")

        then:
        list == [null, null, null, "test", null, null, "atSix"]

        and:
        when:
        CustomPropertiesResolver.addToCollectionAtIndex(list, 2, "atTwo")

        then:
        list == [null, null, "atTwo", "test", null, null, "atSix"]
    }

    def "should add element at first index"() {
        given:
        List<String> list = []

        when:
        CustomPropertiesResolver.addToCollectionAtIndex(list, 0, "zero")

        then:
        list == ["zero"]

        and:
        when:
        CustomPropertiesResolver.addToCollectionAtIndex(list, 1, "one")

        then:
        list == ["zero", "one"]
    }
}

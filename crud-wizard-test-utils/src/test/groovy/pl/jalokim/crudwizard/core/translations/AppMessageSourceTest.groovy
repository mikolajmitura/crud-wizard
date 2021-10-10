package pl.jalokim.crudwizard.core.translations

import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.initStaticAppMessageSource

import spock.lang.Specification
import spock.lang.Unroll

class AppMessageSourceTest extends Specification {

    private AppMessageSource appMessageSource = initStaticAppMessageSource(false, "app_message_source_test")
    private AppMessageSource testCase = new AppMessageSource() {

        @Override
        String getMessage(String code) {
            return appMessageSource.getMessage(code)
        }
    }

    def "should get message for enum"() {
        when:
        def result = testCase.getMessageByEnum(ExampleEnum.TEST)
        then:
        result == "test"
    }

    def "should get property with arguments"() {
        when:
        def result = testCase.getMessage("example.props", "arg1", 5)
        then:
        result == "test arg1 5 arg1"
    }

    def "should get property by class prefix and code with arguments"() {
        when:
        def result = testCase.getMessage(getClass(), "example.code", "arg1", 5)
        then:
        result == "test arg1 5 arg1"
    }

    def "should get property by class prefix and code without arguments"() {
        when:
        def result = testCase.getMessage(getClass(), "example.code.without.args")
        then:
        result == "test value"
    }

    @Unroll
    def "should find property value: #expectedPropertyValue with optionalSuffixes when given: #optionalSuffixes"() {
        when:
        Object[] optionalSuffixesArgs = optionalSuffixes.toArray()
        def result = testCase.getMessageWithOptionalSuffixes("property.root.key", optionalSuffixesArgs)
        then:
        result == expectedPropertyValue
        where:
        expectedPropertyValue        || optionalSuffixes
        "default value for root key" || []
        "node 1"                     || ["node1"]
        "node 1"                     || ["node1", "nodeX"]
        "node 3"                     || ["node1", "node2", "node3"]
        "node 2"                     || ["node1", "node2", "node21", "node23"]
        "default value for root key" || ["node0", "node2", "node21", "node23"]
    }

    @Unroll
    def "should find property value be enum: #expectedPropertyValue with optionalSuffixes when given: #optionalSuffixes"() {
        when:
        Object[] optionalSuffixesArgs = optionalSuffixes.toArray()
        def result = testCase.getMessageByEnumWithSuffixes(ExampleEnum.ENUM2, optionalSuffixesArgs)
        then:
        result == expectedPropertyValue
        where:
        expectedPropertyValue || optionalSuffixes
        "enum2 root"          || []
        "enum2 root"          || [ExampleEnum.ENUM2]
        "enum2 TEST"          || [ExampleEnum.TEST]
        "enum2 TEST node2"    || [ExampleEnum.TEST, "node2"]
    }

    def "should find property value: #expectedPropertyValue by prefix: #propertyPrefix and by enum"() {
        when:
        def result = testCase.getMessageByEnumWithPrefix(propertyPrefix, ExampleEnum.ENUM2, "Arg1")
        then:
        result == expectedPropertyValue
        where:
        expectedPropertyValue || propertyPrefix
        "prefix 1 Enum2 Arg1" || "prefix1"
        "prefix 2 Enum2 Arg1" || "prefix2"
    }
}

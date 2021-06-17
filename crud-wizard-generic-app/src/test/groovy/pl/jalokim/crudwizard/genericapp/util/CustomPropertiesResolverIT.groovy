package pl.jalokim.crudwizard.genericapp.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.core.utils.CustomPropertiesResolver

class CustomPropertiesResolverIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private Environment environment

    def "should bind properties to SomeExampleProperties"() {
        given:
        def someExampleProperties = new SomeExampleProperties()

        when:
        CustomPropertiesResolver.bind(someExampleProperties, environment)

        then:
        someExampleProperties.someText == "someText"
        someExampleProperties.someRawLong == 1
        someExampleProperties.someLong == 2
        someExampleProperties.rawBoolean
        someExampleProperties.objectBoolean
        someExampleProperties.simpleList == ["one", "two"]
        someExampleProperties.listWithObjects == [
            new SomeExampleProperties.SomeObject([
                someDouble : 12.0,
                rawDouble  : 13.0,
                rawShort   : 12,
                objectShort: 13
            ]),
            new SomeExampleProperties.SomeObject([
                someDouble : 15.0,
                rawDouble  : 16.0,
                rawShort   : 15,
                objectShort: 16
            ])
        ]
    }

    def "cannot convert from invalid data"() {
        given:
        def someExampleInvalidProperties = new InvalidFieldValueProperties()

        when:
        CustomPropertiesResolver.bind(someExampleInvalidProperties, environment)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Cannot set field: 'someLong' with type 'java.lang.Long' in class ${InvalidFieldValueProperties.getCanonicalName()}, " +
            "problematic property key: some-example.invalid-field-value.someLong=test from: applicationConfig: [classpath:/application-integration.yaml]"
    }

    def "cannot find field name in some class"() {
        given:
        def invalidFieldNameProperties = new InvalidFieldNameProperties()

        when:
        CustomPropertiesResolver.bind(invalidFieldNameProperties, environment)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "cannot find field 'notExist' in class ${InvalidFieldNameProperties.getCanonicalName()}," +
            " problematic property key: some-example.invalid-field-name.notExist=someValue from: applicationConfig: [classpath:/application.yaml]"
    }
}

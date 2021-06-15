package pl.jalokim.crudwizard.genericapp.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.TestsApplicationConfig
import pl.jalokim.crudwizard.core.utils.CustomPropertiesResolver
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles(["integration"])
@SpringBootTest(classes = [TestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CustomPropertiesResolverIT extends BaseIntegrationSpecification {

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

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.WRITE_FIELD_RESOLVER_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.sample.SomeAllArgConstructor
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.core.sample.SomeSuperAllArgConstructor
import pl.jalokim.utils.reflection.TypeMetadata
import spock.lang.Specification

class ByAllArgsFieldsResolverTest extends Specification {

    ByAllArgsFieldsResolver testCase = new ByAllArgsFieldsResolver()

    def "return expected fields for SomeValueDto"() {
        given:
        TypeMetadata someValueDtoTypeMetadata = getTypeMetadataFromType(SomeSimpleValueDto)

        when:
        def results = testCase.findDeclaredFields(someValueDtoTypeMetadata, WRITE_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 3
        verifyAll(results.find {
            it.fieldName == "someString3"
        }) {
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "someLong3"
        }) {
            fieldType.realClass == Long
        }

        verifyAll(results.find {
            it.fieldName == "someDataTime3"
        }) {
            fieldType.realClass == LocalDateTime
        }
    }

    def "return expected fields for SomeSuperAllArgConstructor"() {
        given:
        TypeMetadata someValueDtoTypeMetadata = getTypeMetadataFromType(SomeAllArgConstructor)

        when:
        def results = testCase.findDeclaredFields(someValueDtoTypeMetadata, WRITE_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 4
        verifyAll(results.find {
            it.fieldName == "type1"
        }) {
            fieldType.realClass == Long
        }

        verifyAll(results.find {
            it.fieldName == "name"
        }) {
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "taste"
        }) {
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "notField"
        }) {
            fieldType.realClass == Long
        }
    }

    def "cannot find one constructor"() {
        given:
        TypeMetadata typeMetadata = getTypeMetadataFromType(SomeSuperAllArgConstructor)

        when:
        testCase.findDeclaredFields(typeMetadata, WRITE_FIELD_RESOLVER_CONFIG)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "given class ${typeMetadata.getRawType()} should have one constructor with max number of arguments"
    }
}

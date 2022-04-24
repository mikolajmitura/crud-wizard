package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.utils.reflection.TypeMetadata
import spock.lang.Specification

class ByDeclaredFieldsResolverTest extends Specification {

    ByDeclaredFieldsResolver testCase = new ByDeclaredFieldsResolver()

    def "return expected list of field metamodels for SomeDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)

        when:
        def results = testCase.findDeclaredFields(someDtoTypeMetadata, READ_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 3
        verifyAll(results[0]) {
            fieldName == "innerSomeDto"
            fieldType.realClass == SomeDto
        }
        verifyAll(results[1]) {
            fieldName == "someOtherMap"
            fieldType.realClass == Map
        }
        verifyAll(results[2]) {
            fieldName == "someId"
            fieldType.realClass == Long
        }
    }

    def "return expected list of field metamodels for SomeMiddleGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()

        when:
        def results = testCase.findDeclaredFields(someMiddleGenericDtoMetadata, READ_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 2
        verifyAll(results[0]) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }
        verifyAll(results[1]) {
            fieldName == "someOtherMiddleField"
            fieldType.realClass == Long
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def SuperGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()

        when:
        def results = testCase.findDeclaredFields(SuperGenericDtoMetadata, READ_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 3
        verifyAll(results[0]) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(results[1]) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(results[2]) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
            fieldType.genericTypes*.realClass == [String, Map]
            fieldType.genericTypes[1].genericTypes*.realClass == [Long, String]
        }
    }
}

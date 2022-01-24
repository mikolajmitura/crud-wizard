package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration
import pl.jalokim.utils.reflection.TypeMetadata
import spock.lang.Specification

class ByDeclaredFieldsResolverTest extends Specification {

    ByDeclaredFieldsResolver testCase = new ByDeclaredFieldsResolver()

    def "return expected list of field metamodels for SomeDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)

        when:
        def results = testCase.findDeclaredFields(someDtoTypeMetadata, new FieldMetaResolverConfiguration(READ))

        then:
        results.size() == 3
        verifyAll(results[0]) {
            fieldName == "innerSomeDto"
            fieldType.realClass == SomeDto
            fieldType.name == "someDto"
        }
        verifyAll(results[1]) {
            fieldName == "someOtherMap"
            fieldType.realClass == Map
            fieldType.name == "map_String_Object_someOtherMap_SomeDto"
        }
        verifyAll(results[2]) {
            fieldName == "someId"
            fieldType.realClass == Long
            fieldType.name == null
        }
    }

    def "return expected list of field metamodels for SomeMiddleGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()

        when:
        def results = testCase.findDeclaredFields(someMiddleGenericDtoMetadata, new FieldMetaResolverConfiguration(READ))

        then:
        results.size() == 2
        verifyAll(results[0]) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
            fieldType.name == "someDto"
        }
        verifyAll(results[1]) {
            fieldName == "someOtherMiddleField"
            fieldType.realClass == Long
            fieldType.name == null
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def SuperGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()

        when:
        def results = testCase.findDeclaredFields(SuperGenericDtoMetadata, new FieldMetaResolverConfiguration(READ))

        then:
        results.size() == 3
        verifyAll(results[0]) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.name == "list_SomeDto_someListOfT_SuperGenericDto"
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(results[1]) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.name == "set_Long_objectOfIType_SuperGenericDto"
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(results[2]) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
            fieldType.name == "map_String_Map_mapWithSType_SuperGenericDto"
            fieldType.genericTypes*.realClass == [String, Map]
            fieldType.genericTypes[1].genericTypes*.realClass == [Long, String]
        }
    }
}

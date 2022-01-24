package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration
import pl.jalokim.utils.reflection.TypeMetadata
import spock.lang.Specification

class ByGettersFieldsResolverTest extends Specification {

    ByGettersFieldsResolver testCase = new ByGettersFieldsResolver()

    def "return expected list of field metamodels for SomeDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)

        when:
        def results = testCase.findDeclaredFields(someDtoTypeMetadata, new FieldMetaResolverConfiguration(READ))

        then:
        results.size() == 1
        verifyAll(results[0]) {
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
        results.size() == 3
        verifyAll(results.find {
            it.fieldName == "someString"
        }) {
            fieldName == "someString"
            fieldType.realClass == String
            fieldType.name == null
        }
        verifyAll(results.find {
            it.fieldName == "someLong"
        }) {
            fieldName == "someLong"
            fieldType.realClass == Long
            fieldType.name == null
        }
        verifyAll(results.find {
            it.fieldName == "objectOfMiddle"
        }) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
            fieldType.name == "someDto"
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def superGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()

        when:
        def results = testCase.findDeclaredFields(superGenericDtoMetadata, new FieldMetaResolverConfiguration(READ))

        then:
        results.size() == 4
        verifyAll(results.find {
            it.fieldName == "someListOfT"
        }) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.name == "list_SomeDto_someListOfT_SuperGenericDto"
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(results.find {
            it.fieldName == "objectOfIType"
        }) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.name == "set_Long_objectOfIType_SuperGenericDto"
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(results.find {
            it.fieldName == "copyOfObjectOfTType"
        }) {
            fieldName == "copyOfObjectOfTType"
            fieldType.realClass == Set
            fieldType.name == "set_Long_copyOfObjectOfTType_SuperGenericDto"
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(results.find {
            it.fieldName == "results2"
        }) {
            fieldName == "results2"
            fieldType.realClass == Long
            fieldType.name == null
        }
    }
}

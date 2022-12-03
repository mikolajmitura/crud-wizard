package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.utils.reflection.TypeMetadata

// TODO #1 fix this test in future, is not working on github.
class BySettersFieldsResolverTest {

    BySettersFieldsResolver testCase = new BySettersFieldsResolver()

    def "return expected list of field metamodels for SomeDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)

        when:
        def results = testCase.findDeclaredFields(someDtoTypeMetadata, READ_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 1
        verifyAll(results[0]) {
            fieldName == "innerSomeDto"
            fieldType.realClass == SomeDto
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
        verifyAll(results.find {
            it.fieldName == "myString"
        }) {
            fieldName == "myString"
            fieldType.realClass == String
        }
        verifyAll(results.find {
            it.fieldName == "objectOfMiddle"
        }) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def superGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()

        when:
        def results = testCase.findDeclaredFields(superGenericDtoMetadata, READ_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 3
        verifyAll(results.find {
            it.fieldName == "someListOfT"
        }) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(results.find {
            it.fieldName == "objectOfIType"
        }) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(results.find {
            it.fieldName == "mapWithSType"
        }) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
            fieldType.genericTypes*.realClass == [String, Map]
        }
    }
}

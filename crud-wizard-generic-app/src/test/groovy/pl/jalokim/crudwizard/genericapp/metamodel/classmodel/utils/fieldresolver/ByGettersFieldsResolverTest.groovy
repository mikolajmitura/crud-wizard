package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.utils.reflection.TypeMetadata

class ByGettersFieldsResolverTest extends FieldsResolverSpecification {

    ByGettersFieldsResolver testCase = new ByGettersFieldsResolver()

    def setup() {
        ClassMetaModelFactory.clearCache()
    }

    def "return expected list of field metamodels for SomeDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(someDtoTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveReadFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def currentFields = classMetaModel.getFields().findAll {
            it.isReadField()
        }
        currentFields.size() == 1
        verifyAll(currentFields[0]) {
            fieldName == "someId"
            fieldType.realClass == Long
            isReadField()
        }

        def results = classMetaModel.fetchAllReadFields()
        results.size() == 8
        verifyAll(findField(results, "someId")) {
            fieldName == "someId"
            fieldType.realClass == Long
            isReadField()
        }

        verifyAll(findField(results, "someString")) {
            fieldName == "someString"
            fieldType.realClass == String
        }
        verifyAll(findField(results, "someLong")) {
            fieldName == "someLong"
            fieldType.realClass == Long
        }
        verifyAll(findField(results, "objectOfMiddle")) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }

        verifyAll(findField(results, "someListOfT")) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(findField(results, "objectOfIType")) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(results, "copyOfObjectOfTType")) {
            fieldName == "copyOfObjectOfTType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(results, "results2")) {
            fieldName == "results2"
            fieldType.realClass == Long
        }
    }

    def "return expected list of field metamodels for SomeMiddleGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(someMiddleGenericDtoMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveReadFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def currentFields = classMetaModel.getFields().findAll {
            it.isReadField()
        }
        currentFields.size() == 3
        verifyAll(findField(currentFields, "someString")) {
            fieldName == "someString"
            fieldType.realClass == String
        }
        verifyAll(findField(currentFields, "someLong")) {
            fieldName == "someLong"
            fieldType.realClass == Long
        }
        verifyAll(findField(currentFields, "objectOfMiddle")) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }

        def results = classMetaModel.fetchAllReadFields()
        results.size() == 7
        verifyAll(findField(results, "someString")) {
            fieldName == "someString"
            fieldType.realClass == String
        }
        verifyAll(findField(results, "someLong")) {
            fieldName == "someLong"
            fieldType.realClass == Long
        }
        verifyAll(findField(results, "objectOfMiddle")) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }

        verifyAll(findField(results, "someListOfT")) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(findField(results, "objectOfIType")) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(results, "copyOfObjectOfTType")) {
            fieldName == "copyOfObjectOfTType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(results, "results2")) {
            fieldName == "results2"
            fieldType.realClass == Long
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def superGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(superGenericDtoMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveReadFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def results = classMetaModel.fetchAllReadFields()
        results == classMetaModel.getFields().findAll {
            it.isReadField()
        }
        results.size() == 4
        verifyAll(findField(results, "someListOfT")) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(findField(results, "objectOfIType")) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(results, "copyOfObjectOfTType")) {
            fieldName == "copyOfObjectOfTType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(results, "results2")) {
            fieldName == "results2"
            fieldType.realClass == Long
        }
    }
}

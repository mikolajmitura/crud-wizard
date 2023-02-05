package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.utils.reflection.TypeMetadata

class BySettersFieldsResolverTest extends FieldsResolverSpecification {

    BySettersFieldsResolver testCase = new BySettersFieldsResolver()

    def "return expected list of field metamodels for SomeDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(someDtoTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def currentFields = classMetaModel.getFields().findAll {
            it.isWriteField()
        }
        currentFields.size() == 1
        verifyAll(currentFields[0]) {
            fieldName == "innerSomeDto"
            fieldType.realClass == SomeDto
        }

        def results = classMetaModel.fetchAllWriteFields()
        results.size() == 6
        verifyAll(findField(results, "innerSomeDto")) {
            fieldName == "innerSomeDto"
            fieldType.realClass == SomeDto
        }

        verifyAll(findField(results, "myString")) {
            fieldName == "myString"
            fieldType.realClass == String
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
        verifyAll(findField(results, "mapWithSType")) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
            fieldType.genericTypes*.realClass == [String, Map]
        }
    }

    def "return expected list of field metamodels for SomeMiddleGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(someMiddleGenericDtoMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def currentFields = classMetaModel.getFields().findAll {
            it.isWriteField()
        }
        currentFields.size() == 2
        verifyAll(findField(currentFields, "myString")) {
            fieldName == "myString"
            fieldType.realClass == String
        }
        verifyAll(findField(currentFields, "objectOfMiddle")) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }

        def results = classMetaModel.fetchAllWriteFields()
        results.size() == 5

        verifyAll(findField(results, "myString")) {
            fieldName == "myString"
            fieldType.realClass == String
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
        verifyAll(findField(results, "mapWithSType")) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
            fieldType.genericTypes*.realClass == [String, Map]
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def superGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(superGenericDtoMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def currentFields = classMetaModel.getFields().findAll {
            it.isWriteField()
        }
        currentFields == classMetaModel.fetchAllWriteFields()
        currentFields.size() == 3
        verifyAll(findField(currentFields, "someListOfT")) {
            fieldName == "someListOfT"
            fieldType.realClass == List
            fieldType.genericTypes*.realClass == [SomeDto]
        }
        verifyAll(findField(currentFields, "objectOfIType")) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
            fieldType.genericTypes*.realClass == [Long]
        }
        verifyAll(findField(currentFields, "mapWithSType")) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
            fieldType.genericTypes*.realClass == [String, Map]
        }
    }
}

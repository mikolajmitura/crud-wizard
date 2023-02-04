package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.core.sample.SomeMiddleGenericDto
import pl.jalokim.crudwizard.core.sample.SuperGenericDto
import pl.jalokim.utils.reflection.TypeMetadata

class ByDeclaredFieldsResolverTest extends FieldsResolverSpecification {

    ByDeclaredFieldsResolver testCase = new ByDeclaredFieldsResolver()

    def "return expected list of field metamodels for SomeDto"() {
        given:
        def fieldResolversConf = FieldMetaResolverConfiguration.builder()
            .writeFieldMetaResolverForClass(Map.of(SomeDto, testCase))
            .build()
        def someDtoMetamodel = createClassMetaModel(getTypeMetadataFromType(SomeDto), fieldResolversConf)

        when:
        testCase.resolveWriteFields(someDtoMetamodel, fieldResolversConf)

        then:
        def results = someDtoMetamodel.getFields()
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
        someDtoMetamodel.fetchAllWriteFields().size() == 8
    }

    def "return expected list of field metamodels for SomeMiddleGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def fieldResolversConf = FieldMetaResolverConfiguration.builder()
            .writeFieldMetaResolverForClass(Map.of(SomeMiddleGenericDto, testCase))
            .build()
        def someMiddleGenericDtoMetaModel = createClassMetaModel(someDtoTypeMetadata.getParentTypeMetadata(), fieldResolversConf)

        when:
        testCase.resolveWriteFields(someMiddleGenericDtoMetaModel, fieldResolversConf)

        then:
        def results = someMiddleGenericDtoMetaModel.getFields()
        results.size() == 2
        verifyAll(results[0]) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }
        verifyAll(results[1]) {
            fieldName == "someOtherMiddleField"
            fieldType.realClass == Long
        }
        def allWriteFields = someMiddleGenericDtoMetaModel.fetchAllWriteFields()
        allWriteFields.size() == 5

        verifyAll(findField(allWriteFields, "someOtherMiddleField")) {
            fieldName == "someOtherMiddleField"
            fieldType.realClass == Long
        }

        verifyAll(findField(allWriteFields, "objectOfMiddle")) {
            fieldName == "objectOfMiddle"
            fieldType.realClass == SomeDto
        }

        verifyAll(findField(allWriteFields, "someListOfT")) {
            fieldName == "someListOfT"
            fieldType.realClass == List
        }

        verifyAll(findField(allWriteFields, "objectOfIType")) {
            fieldName == "objectOfIType"
            fieldType.realClass == Set
        }

        verifyAll(findField(allWriteFields, "mapWithSType")) {
            fieldName == "mapWithSType"
            fieldType.realClass == Map
        }
    }

    def "return expected list of field metamodels for SuperGenericDto"() {
        given:
        TypeMetadata someDtoTypeMetadata = getTypeMetadataFromType(SomeDto)
        def someMiddleGenericDtoMetadata = someDtoTypeMetadata.getParentTypeMetadata()
        def superGenericDtoMetadata = someMiddleGenericDtoMetadata.getParentTypeMetadata()

        def fieldResolversConf = FieldMetaResolverConfiguration.builder()
            .writeFieldMetaResolverForClass(Map.of(SuperGenericDto, testCase))
            .build()
        def superGenericDtoMetaModel = createClassMetaModel(superGenericDtoMetadata, fieldResolversConf)

        when:
        testCase.resolveWriteFields(superGenericDtoMetaModel, fieldResolversConf)

        then:
        def results = superGenericDtoMetaModel.getFields()
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
        superGenericDtoMetaModel.fetchAllWriteFields().size() == 3
    }
}

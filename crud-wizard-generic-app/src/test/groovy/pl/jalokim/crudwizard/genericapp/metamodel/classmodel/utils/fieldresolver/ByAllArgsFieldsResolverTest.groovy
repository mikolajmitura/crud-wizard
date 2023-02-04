package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.sample.SomeAllArgConstructor
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.core.sample.SomeSuperAllArgConstructor
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.utils.reflection.TypeMetadata

class ByAllArgsFieldsResolverTest extends FieldsResolverSpecification {

    ByAllArgsFieldsResolver testCase = new ByAllArgsFieldsResolver()

    def "return expected fields for SomeSimpleValueDto"() {
        given:
        TypeMetadata someValueDtoTypeMetadata = getTypeMetadataFromType(SomeSimpleValueDto)
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(someValueDtoTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def results = classMetaModel.fetchAllWriteFields()
        results.size() == 3
        results == classMetaModel.getFields().findAll {
            it.isWriteField()
        }

        verifyAll(findField(results, "someString3")) {
            fieldType.realClass == String
            isWriteField()
        }

        verifyAll(findField(results, "someLong3")) {
            fieldType.realClass == Long
            isWriteField()
        }

        verifyAll(findField(results, "someDataTime3")) {
            fieldType.realClass == LocalDateTime
            isWriteField()
        }
    }

    def "return expected fields for SomeAllArgConstructor"() {
        given:
        TypeMetadata someValueDtoTypeMetadata = getTypeMetadataFromType(SomeAllArgConstructor)
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(someValueDtoTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def results = classMetaModel.getFields()
        def allWriteFields = classMetaModel.fetchAllWriteFields()
        allWriteFields as Set == results as Set
        results.size() == 4
        verifyAll(findField(results, "type1")) {
            fieldType.realClass == Long
            isWriteField()
        }

        verifyAll(findField(results, "name")) {
            fieldType.realClass == String
            isWriteField()
        }

        verifyAll(findField(results, "taste")) {
            fieldType.realClass == String
            isWriteField()
        }

        verifyAll(findField(results, "notField")) {
            fieldType.realClass == Long
            isWriteField()
        }
    }

    def "cannot find one constructor"() {
        given:
        TypeMetadata typeMetadata = getTypeMetadataFromType(SomeSuperAllArgConstructor)
        ClassMetaModel classMetaModel = ClassMetaModelFactory.createClassMetaModel(typeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "given class ${typeMetadata.getRawType()} should have one constructor with max number of arguments"
    }
}

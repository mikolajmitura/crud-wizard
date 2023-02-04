package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType

import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.sample.SomeDtoWithBuilder
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSuperBuilder
import pl.jalokim.crudwizard.core.sample.SuperDtoWithSuperBuilder
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.utils.reflection.TypeMetadata

class ByBuilderFieldsResolverTest extends FieldsResolverSpecification {

    ByBuilderFieldsResolver testCase = new ByBuilderFieldsResolver()

    def "return expected list of fields for SomeDtoWithSuperBuilder"() {
        given:
        TypeMetadata someDtoWithBuilderTypeMetadata = getTypeMetadataFromType(SomeDtoWithSuperBuilder)
        ClassMetaModel classMetaModel = createClassMetaModel(someDtoWithBuilderTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def results = classMetaModel.fetchAllWriteFields()
        results.size() == 4
        verifyAll(findField(results, "someString1")) {
            fieldName == "someString1"
            fieldType.realClass == String
        }
        verifyAll(findField(results, "someLong1")) {
            fieldName == "someLong1"
            fieldType.realClass == Long
        }
        verifyAll(findField(results, "superStringField")) {
            fieldName == "superStringField"
            fieldType.realClass == String
        }
        verifyAll(findField(results, "someMap")) {
            fieldName == "someMap"
            fieldType.realClass == Map
        }
    }

    def "return expected list of fields for SuperDtoWithSuperBuilder"() {
        given:
        TypeMetadata superDtoWithSuperBuilderTypeMetadata = getTypeMetadataFromType(SuperDtoWithSuperBuilder)
        ClassMetaModel classMetaModel = createClassMetaModel(superDtoWithSuperBuilderTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def results = classMetaModel.fetchAllWriteFields()
        results.size() == 2
        verifyAll(findField(results, "superStringField")) {
            fieldName == "superStringField"
            fieldType.realClass == String
        }
        verifyAll(findField(results, "someMap")) {
            fieldName == "someMap"
            fieldType.getJavaGenericTypeInfo() == "java.util.Map<java.lang.String, java.util.List<java.lang.Long>>"
        }
    }

    def "return expected list of fields for SomeDtoWithBuilder"() {
        given:
        TypeMetadata superDtoWithSuperBuilderTypeMetadata = getTypeMetadataFromType(SomeDtoWithBuilder)
        ClassMetaModel classMetaModel = createClassMetaModel(superDtoWithSuperBuilderTypeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG)

        when:
        testCase.resolveWriteFields(classMetaModel, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def results = classMetaModel.fetchAllWriteFields()
        results.size() == 3
        verifyAll(findField(results, "test1")) {
            fieldName == "test1"
            fieldType.realClass == String
        }

        verifyAll(findField(results, "testLong1")) {
            fieldName == "testLong1"
            fieldType.realClass == Long
        }

        verifyAll(findField(results, "localDateTime1")) {
            fieldName == "localDateTime1"
            fieldType.realClass == LocalDateTime
        }
    }
}

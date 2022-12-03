package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.WRITE_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.generateGenericClassMetaModel
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
        TypeMetadata SomeDtoWithBuilderTypeMetadata = getTypeMetadataFromType(SomeDtoWithSuperBuilder)

        when:
        def results = testCase.findDeclaredFields(SomeDtoWithBuilderTypeMetadata, WRITE_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 2
        verifyAll(results.find {
            it.fieldName == "someString1"
        }) {
            fieldName == "someString1"
            fieldType.realClass == String
        }
        verifyAll(results.find {
            it.fieldName == "someLong1"
        }) {
            fieldName == "someLong1"
            fieldType.realClass == Long
        }
    }

    def "return expected list of fields for SuperDtoWithSuperBuilder"() {
        given:
        TypeMetadata superDtoWithSuperBuilderTypeMetadata = getTypeMetadataFromType(SuperDtoWithSuperBuilder)

        when:
        def results = testCase.findDeclaredFields(superDtoWithSuperBuilderTypeMetadata, WRITE_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 2
        verifyAll(results.find {
            it.fieldName == "superStringField"
        }) {
            fieldName == "superStringField"
            fieldType.realClass == String
        }
        verifyAll(results.find {
            it.fieldName == "someMap"
        }) {
            fieldName == "someMap"
            fieldType.realClass == Map
            fieldType.genericTypes*.realClass == [String, List]
            fieldType.genericTypes[1].genericTypes*.realClass == [Long]
        }
    }

    def "return expected list of fields for SomeDtoWithBuilder"() {
        given:
        TypeMetadata superDtoWithSuperBuilderTypeMetadata = getTypeMetadataFromType(SomeDtoWithBuilder)

        when:
        def results = testCase.findDeclaredFields(superDtoWithSuperBuilderTypeMetadata, WRITE_FIELD_RESOLVER_CONFIG)

        then:
        results.size() == 3

        verifyAll(results.find {
            it.fieldName == "test1"
        }) {
            fieldName == "test1"
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "testLong1"
        }) {
            fieldName == "testLong1"
            fieldType.realClass == Long
        }

        verifyAll(results.find {
            it.fieldName == "localDateTime1"
        }) {
            fieldName == "localDateTime1"
            fieldType.realClass == LocalDateTime
        }
    }

    def "return all available fields for SomeDtoWithSuperBuilder"() {
        given:
        ClassMetaModel classMetaModel = generateGenericClassMetaModel(SomeDtoWithSuperBuilder.class, WRITE_FIELD_RESOLVER_CONFIG)

        when:
        def results = testCase.getAllAvailableFieldsForWrite(classMetaModel)

        then:
        results.size() == 4

        verifyAll(results.find {
            it.fieldName == "someString1"
        }) {
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "someLong1"
        }) {
            fieldType.realClass == Long
        }

        verifyAll(results.find {
            it.fieldName == "superStringField"
        }) {
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "someMap"
        }) {
            fieldType.realClass == Map
        }
    }

    def "return all available fields for SomeDtoWithBuilder"() {
        given:
        ClassMetaModel classMetaModel = generateGenericClassMetaModel(SomeDtoWithBuilder.class, WRITE_FIELD_RESOLVER_CONFIG)

        when:
        def results = testCase.getAllAvailableFieldsForWrite(classMetaModel)

        then:
        results.size() == 3

        verifyAll(results.find {
            it.fieldName == "test1"
        }) {
            fieldType.realClass == String
        }

        verifyAll(results.find {
            it.fieldName == "testLong1"
        }) {
            fieldType.realClass == Long
        }

        verifyAll(results.find {
            it.fieldName == "localDateTime1"
        }) {
            fieldType.realClass == LocalDateTime
        }
    }
}

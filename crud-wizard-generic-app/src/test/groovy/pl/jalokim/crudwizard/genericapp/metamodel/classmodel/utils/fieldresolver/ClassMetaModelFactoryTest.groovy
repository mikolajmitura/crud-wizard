package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.READ
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.WRITE
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.WRITE_READ
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.core.sample.SomeMiddleGenericDto
import pl.jalokim.crudwizard.core.sample.SuperGenericDto
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample.BeanWithJsonProperties
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample.ConcreteDto1
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample.ThirdLevelClass
import spock.lang.Unroll

class ClassMetaModelFactoryTest extends FieldsResolverSpecification {

    def "return expected ClassMetaModel from SomeDto"() {
        when:
        def classMetaModel = createClassMetaModel(SomeDto, FieldMetaResolverConfiguration.builder().build()
            .putWriteFieldResolver(SomeDto, ByDeclaredFieldsResolver.INSTANCE)
            .putWriteFieldResolver(SomeMiddleGenericDto, BySettersFieldsResolver.INSTANCE)
            .putWriteFieldResolver(SuperGenericDto, BySettersFieldsResolver.INSTANCE)
        )

        then:
        verifyAll(classMetaModel) {
            name == null
            className == SomeDto.canonicalName
            realClass == SomeDto
            genericTypes.isEmpty()
            fields.size() == 3
            fetchAllFields().size() == 12
            fetchAllWriteFields().size() == 8
            fetchAllReadFields().size() == 8
            extendsFromModels.size() == 1

            verifyAll(getFieldByName("innerSomeDto")) {
                fieldName == "innerSomeDto"
                fieldType == classMetaModel
                isWriteField()
                !isReadField()
            }

            verifyAll(getFieldByName("someOtherMap")) {
                fieldName == "someOtherMap"
                fieldType.realClass == Map
                fieldType.genericTypes*.realClass == [String, Object]
                isWriteField()
                !isReadField()
            }

            verifyAll(getFieldByName("someId")) {
                fieldName == "someId"
                fieldType.realClass == Long
                isReadField()
                isWriteField()
            }

            verifyAll(getFieldByName("objectOfMiddle")) {
                fieldName == "objectOfMiddle"
                fieldType.realClass == SomeDto
                isReadField()
                isWriteField()
            }

            verifyAll(getFieldByName("someOtherMiddleField")) {
                fieldName == "someOtherMiddleField"
                fieldType.realClass == Long
                !isReadField()
                isWriteField()
            }

            verifyAll(getFieldByName("someString")) {
                fieldName == "someString"
                fieldType.realClass == String
                isReadField()
                !isWriteField()
            }

            verifyAll(getFieldByName("someLong")) {
                fieldName == "someLong"
                fieldType.realClass == Long
                isReadField()
                !isWriteField()
            }

            getFieldByName("myString") == null

            verifyAll(getFieldByName("someListOfT")) {
                fieldName == "someListOfT"
                fieldType.realClass == List
                fieldType.genericTypes == [classMetaModel]
                isReadField()
                isWriteField()
            }

            verifyAll(getFieldByName("objectOfIType")) {
                fieldName == "objectOfIType"
                fieldType.realClass == Set
                fieldType.genericTypes*.realClass == [Long]
                isReadField()
                isWriteField()
            }

            verifyAll(getFieldByName("mapWithSType")) {
                fieldName == "mapWithSType"
                fieldType.realClass == Map
                fieldType.genericTypes*.realClass == [String, Map]
                fieldType.genericTypes[1].genericTypes*.realClass == [Long, String]
                !isReadField()
                isWriteField()
            }

            verifyAll(extendsFromModels[0]) {
                name == null
                className == SomeMiddleGenericDto.canonicalName
                realClass == SomeMiddleGenericDto
                genericTypes == [classMetaModel]
                fields.size() == 4
                fetchAllFields().size() == 10
                extendsFromModels.size() == 1

                verifyAll(getFieldByName("objectOfMiddle")) {
                    fieldName == "objectOfMiddle"
                    fieldType.realClass == SomeDto
                    isReadField()
                    isWriteField()
                }

                verifyAll(getFieldByName("someOtherMiddleField")) {
                    fieldName == "someOtherMiddleField"
                    fieldType.realClass == Long
                    !isReadField()
                    isWriteField()
                }

                verifyAll(getFieldByName("someString")) {
                    fieldName == "someString"
                    fieldType.realClass == String
                    isReadField()
                    !isWriteField()
                }

                verifyAll(getFieldByName("someLong")) {
                    fieldName == "someLong"
                    fieldType.realClass == Long
                    isReadField()
                    !isWriteField()
                }

                verifyAll(getFieldByName("myString")) {
                    fieldName == "myString"
                    fieldType.realClass == String
                    !isReadField()
                    isWriteField()
                }

                verifyAll(getFieldByName("someListOfT")) {
                    fieldName == "someListOfT"
                    fieldType.realClass == List
                    fieldType.genericTypes == [classMetaModel]
                    isReadField()
                    isWriteField()
                }

                verifyAll(getFieldByName("objectOfIType")) {
                    fieldName == "objectOfIType"
                    fieldType.realClass == Set
                    fieldType.genericTypes*.realClass == [Long]
                    isReadField()
                    isWriteField()
                }

                verifyAll(getFieldByName("mapWithSType")) {
                    fieldName == "mapWithSType"
                    fieldType.realClass == Map
                    fieldType.genericTypes*.realClass == [String, Map]
                    fieldType.genericTypes[1].genericTypes*.realClass == [Long, String]
                    !isReadField()
                    isWriteField()
                }

                verifyAll(extendsFromModels[0]) {
                    name == null
                    className == SuperGenericDto.canonicalName
                    realClass == SuperGenericDto
                    genericTypes*.getTypeDescription() == ["pl.jalokim.crudwizard.core.sample.SomeDto", "java.util.Set<java.lang.Long>", "java.lang.String"]
                    fields.size() == 5
                    fetchAllFields().size() == 5
                    extendsFromModels.isEmpty()

                    verifyAll(getFieldByName("someListOfT")) {
                        fieldName == "someListOfT"
                        fieldType.realClass == List
                        fieldType.genericTypes == [classMetaModel]
                        isReadField()
                        isWriteField()
                    }

                    verifyAll(getFieldByName("objectOfIType")) {
                        fieldName == "objectOfIType"
                        fieldType.realClass == Set
                        fieldType.genericTypes*.realClass == [Long]
                        isReadField()
                        isWriteField()
                    }

                    verifyAll(getFieldByName("mapWithSType")) {
                        fieldName == "mapWithSType"
                        fieldType.realClass == Map
                        fieldType.genericTypes*.realClass == [String, Map]
                        fieldType.genericTypes[1].genericTypes*.realClass == [Long, String]
                        !isReadField()
                        isWriteField()
                    }

                    verifyAll(getFieldByName("copyOfObjectOfTType")) {
                        fieldName == "copyOfObjectOfTType"
                        fieldType.realClass == Set
                        fieldType.genericTypes*.realClass == [Long]
                        isReadField()
                        !isWriteField()
                    }

                    verifyAll(getFieldByName("results2")) {
                        fieldName == "results2"
                        fieldType.realClass == Long
                        isReadField()
                        !isWriteField()
                    }
                }
            }
        }
    }

    def "return expected fields for ThirdLevelClass when should find for setters for write and by declared fields for read"() {
        when:
        def thirdLevelClassModel = createClassMetaModel(ThirdLevelClass, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        thirdLevelClassModel.fetchAllWriteFields().size() == 2

        and:
        def twoLevelClassModel = thirdLevelClassModel.getExtendsFromModels()[0]
        def superClassModel = twoLevelClassModel.getExtendsFromModels()[0]

        then:
        // every class have own view of fields and of super class fields
        superClassModel.fetchAllWriteFields().size() == 2
        twoLevelClassModel.fetchAllWriteFields().size() == 4
        superClassModel.fetchAllWriteFields().size() == 2
        twoLevelClassModel.fetchAllWriteFields().size() == 4
        thirdLevelClassModel.fetchAllWriteFields().size() == 2
        twoLevelClassModel.fetchAllWriteFields().size() == 4
        superClassModel.fetchAllWriteFields().size() == 2
    }

    def "return expected fields for ConcreteDto1"() {
        when:
        def concreteDto1MetaModel = createClassMetaModel(ConcreteDto1, DEFAULT_FIELD_RESOLVERS_CONFIG)

        then:
        def allWriteFields = concreteDto1MetaModel.fetchAllWriteFields()
        allWriteFields.size() == 3

        verifyAll(findField(allWriteFields, "str1")) {
            fieldName == "str1"
            fieldType.realClass == String
            isReadField()
            isWriteField()
        }

        verifyAll(findField(allWriteFields, "str2")) {
            fieldName == "str2"
            fieldType.realClass == String
            isReadField()
            isWriteField()
        }

        verifyAll(findField(allWriteFields, "id")) {
            fieldName == "id"
            fieldType.realClass == Long
            isReadField()
            isWriteField()
        }

        def allReadFields = concreteDto1MetaModel.fetchAllReadFields()
        allReadFields.size() == 3

        verifyAll(findField(allReadFields, "str1")) {
            fieldName == "str1"
            fieldType.realClass == String
            isReadField()
            isWriteField()
        }

        verifyAll(findField(allReadFields, "str2")) {
            fieldName == "str2"
            fieldType.realClass == String
            isReadField()
            isWriteField()
        }

        verifyAll(findField(allReadFields, "id")) {
            fieldName == "id"
            fieldType.realClass == Long
            isReadField()
            isWriteField()
        }

        and:
        def superDto1ClassModel = concreteDto1MetaModel.getExtendsFromModels()[0]

        then:
        def allWriteFields2 = superDto1ClassModel.fetchAllWriteFields()
        allWriteFields2.size() == 2
        verifyAll(findField(allWriteFields2, "str1")) {
            fieldName == "str1"
            fieldType.realClass == String
            !isReadField()
            isWriteField()
        }

        verifyAll(findField(allWriteFields2, "str2")) {
            fieldName == "str2"
            fieldType.realClass == String
            isReadField()
            isWriteField()
        }

        def allReadFields2 = superDto1ClassModel.fetchAllReadFields()
        allReadFields2.size() == 1

        verifyAll(findField(allReadFields2, "str2")) {
            fieldName == "str2"
            fieldType.realClass == String
            isReadField()
            isWriteField()
        }
    }

    @Unroll
    def "should return expected additional properties on fields for json name field"() {
        given:
        def concreteDto1MetaModel = createClassMetaModel(BeanWithJsonProperties, FieldMetaResolverConfiguration.builder().build()
            .putWriteFieldResolver(BeanWithJsonProperties, writeFieldResolver)
            .putReadFieldResolver(BeanWithJsonProperties, readFieldResolver)
        )

        when:
        def fields = concreteDto1MetaModel.fetchAllFields()

        then:
        fields.size() == expectedFields.size()
        expectedFields.each {expectedField ->
            def foundField = concreteDto1MetaModel.getFieldByName(expectedField.fieldName)
            verifyAll(foundField) {
                fieldName == expectedField.fieldName
                accessFieldType == expectedField.accessFieldType
                fieldType.realClass == expectedField.fieldType.realClass
                additionalProperties as Set == expectedField.additionalProperties as Set
            }
        }

        where:
        writeFieldResolver                | readFieldResolver                 | expectedFields
        ByDeclaredFieldsResolver.INSTANCE | ByGettersFieldsResolver.INSTANCE  | [
            createField("field1", WRITE_READ, String, [jsonFieldName(READ, "Field_1"), jsonFieldName(WRITE, "Field_1")]),
            createField("fieldNumber3", WRITE_READ, String, [jsonFieldName(READ, '$fieldNumber3_g')]),
            createField("fieldNumber2", WRITE_READ, String),
            createField("id", READ, Long, [jsonFieldName(READ, '$root_id')]),
        ]

        ByAllArgsFieldsResolver.INSTANCE  | ByGettersFieldsResolver.INSTANCE  | [
            createField("field1", WRITE_READ, String, [jsonFieldName(READ, "Field_1")]),
            createField("fieldNumber3", WRITE_READ, String, [jsonFieldName(READ, '$fieldNumber3_g')]),
            createField("fieldNumber2", WRITE_READ, String, [jsonFieldName(WRITE, '$fieldNumber2')]),
            createField("id", READ, Long, [jsonFieldName(READ, '$root_id')]),
        ]

        ByBuilderFieldsResolver.INSTANCE  | ByGettersFieldsResolver.INSTANCE  | [
            createField("field1", WRITE_READ, String, [jsonFieldName(WRITE, "Field_1"), jsonFieldName(READ, "Field_1")]),
            createField("fieldNumber3", WRITE_READ, String, [jsonFieldName(READ, '$fieldNumber3_g')]),
            createField("fieldNumber2", WRITE_READ, String, []),
            createField("id", READ, Long, [jsonFieldName(READ, '$root_id')]),
        ]

        BySettersFieldsResolver.INSTANCE  | ByDeclaredFieldsResolver.INSTANCE | [
            createField("field1", WRITE_READ, String, [jsonFieldName(WRITE, "Field_1"), jsonFieldName(READ, "Field_1")]),
            createField("fieldNumber3", WRITE_READ, String, [jsonFieldName(WRITE, '$fieldNumber3_s')]),
            createField("fieldNumber2", WRITE_READ, String, []),
            createField("otherField", WRITE, String, [jsonFieldName(WRITE, '$Other_field')]),
        ]

        ByDeclaredFieldsResolver.INSTANCE | ByDeclaredFieldsResolver.INSTANCE | [
            createField("field1", WRITE_READ, String, [jsonFieldName(WRITE, "Field_1"), jsonFieldName(READ, "Field_1")]),
            createField("fieldNumber3", WRITE_READ, String, []),
            createField("fieldNumber2", WRITE_READ, String, []),
        ]
    }

    private static FieldMetaModel createField(String name, AccessFieldType accessFieldType, Class<?> fieldType,
        List<AdditionalPropertyMetaModel> additionalProperties = []) {

        FieldMetaModel.builder()
            .fieldName(name)
            .accessFieldType(accessFieldType)
            .fieldType(ClassMetaModel.builder()
                .realClass(fieldType)
                .build())
            .additionalProperties(additionalProperties)
            .build()
    }

    private static AdditionalPropertyMetaModel jsonFieldName(AccessFieldType accessFieldType, String jsonFieldName) {
        AdditionalPropertyMetaModel.builder()
            .name("@Json_Field_Name__${accessFieldType}")
            .valueRealClassName(jsonFieldName)
            .build()
    }
}

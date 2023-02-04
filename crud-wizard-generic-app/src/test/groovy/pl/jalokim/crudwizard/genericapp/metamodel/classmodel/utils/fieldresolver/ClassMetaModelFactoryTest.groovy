package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.core.sample.SomeMiddleGenericDto
import pl.jalokim.crudwizard.core.sample.SuperGenericDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample.ConcreteDto1
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample.ThirdLevelClass

class ClassMetaModelFactoryTest extends FieldsResolverSpecification {

    def "return expected ClassMetaModel from SomeDto"() {
        when:
        def classMetaModel = createClassMetaModel(SomeDto, DEFAULT_FIELD_RESOLVERS_CONFIG
        .putWriteFieldResolver(SomeDto, ByDeclaredFieldsResolver.INSTANCE)
        // those below should be skipped during resolving all fields SomeDto
//        .putWriteFieldResolver(SomeMiddleGenericDto, BySettersFieldsResolver.INSTANCE)
//        .putWriteFieldResolver(SuperGenericDto, BySettersFieldsResolver.INSTANCE)
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
}

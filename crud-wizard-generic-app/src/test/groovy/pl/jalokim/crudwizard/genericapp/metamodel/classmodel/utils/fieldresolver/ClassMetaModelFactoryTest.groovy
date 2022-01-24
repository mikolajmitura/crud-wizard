package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.resolveClassMetaModelByClass

import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.core.sample.SomeMiddleGenericDto
import pl.jalokim.crudwizard.core.sample.SuperGenericDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration
import spock.lang.Specification

class ClassMetaModelFactoryTest extends Specification {

    def "return expected ClassMetaModel from SomeDto"() {
        when:
        def classMetaModel = resolveClassMetaModelByClass(SomeDto, new FieldMetaResolverConfiguration(READ)
        .putFieldResolver(SomeDto, ByDeclaredFieldsResolver.INSTANCE)
        .putFieldResolver(SomeMiddleGenericDto, ByDeclaredFieldsResolver.INSTANCE)
        .putFieldResolver(SuperGenericDto, ByDeclaredFieldsResolver.INSTANCE)
        )

        then:
        verifyAll(classMetaModel) {
            name == "someDto"
            className == SomeDto.canonicalName
            realClass == SomeDto
            genericTypes.isEmpty()
            fields.size() == 3
            fetchAllFields().size() == 8
            extendsFromModels.size() == 1

            verifyAll(getFieldByName("innerSomeDto")) {
                fieldName == "innerSomeDto"
                fieldType == classMetaModel
            }

            verifyAll(getFieldByName("someOtherMap")) {
                fieldName == "someOtherMap"
                fieldType.realClass == Map
                fieldType.genericTypes*.realClass == [String, Object]
            }

            verifyAll(getFieldByName("someId")) {
                fieldName == "someId"
                fieldType.realClass == Long
            }

            verifyAll(extendsFromModels[0]) {
                name == "someMiddleGenericDto_SomeDto"
                className == SomeMiddleGenericDto.canonicalName
                realClass == SomeMiddleGenericDto
                genericTypes == [classMetaModel]
                fields.size() == 2
                fetchAllFields().size() == 5
                extendsFromModels.size() == 1

                verifyAll(getFieldByName("objectOfMiddle")) {
                    fieldName == "objectOfMiddle"
                    fieldType == classMetaModel
                }

                verifyAll(getFieldByName("someOtherMiddleField")) {
                    fieldName == "someOtherMiddleField"
                    fieldType.realClass == Long
                    fieldType.genericTypes.isEmpty()
                }

                verifyAll(extendsFromModels[0]) {
                    name == "superGenericDto_SomeDto_Set_String"
                    className == SuperGenericDto.canonicalName
                    realClass == SuperGenericDto
                    genericTypes*.realClass == [SomeDto, Set, String]
                    fields.size() == 3
                    fetchAllFields().size() == 3
                    extendsFromModels.isEmpty()

                    verifyAll(getFieldByName("someListOfT")) {
                        fieldName == "someListOfT"
                        fieldType.realClass == List
                        fieldType.genericTypes == [classMetaModel]
                    }

                    verifyAll(getFieldByName("objectOfIType")) {
                        fieldName == "objectOfIType"
                        fieldType.realClass == Set
                        fieldType.genericTypes*.realClass == [Long]
                    }

                    verifyAll(getFieldByName("mapWithSType")) {
                        fieldName == "mapWithSType"
                        fieldType.realClass == Map
                        fieldType.genericTypes*.realClass == [String, Map]
                        fieldType.genericTypes[1].genericTypes*.realClass == [Long, String]
                    }
                }
            }
        }
    }

}

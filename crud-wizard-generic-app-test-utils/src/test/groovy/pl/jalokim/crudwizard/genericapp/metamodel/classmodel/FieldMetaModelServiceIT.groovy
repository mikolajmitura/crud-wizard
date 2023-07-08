package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.NestedObject2L
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.NestedObject3L
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeDtoWithNestedFields
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SuperClassDto
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SuperNestedObject

class FieldMetaModelServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private FieldMetaModelService fieldMetaModelService

    def "return expected fields for given class"() {
        given:

        when:
        def fieldsDto = fieldMetaModelService.getAllFieldsForRealClass(SomeDtoWithNestedFields)

        then:
        fieldsDto.size() == 9
        verifyAll(findField(fieldsDto, "superId")) {
            fieldType.className == Long.canonicalName
            ownerOfFieldClassName == SuperClassDto.canonicalName
        }
        verifyAll(findField(fieldsDto, "hashValue")) {
            fieldType.className == String.canonicalName
            ownerOfFieldClassName == SuperClassDto.canonicalName
        }
        verifyAll(findField(fieldsDto, "uuid")) {
            fieldType.className == String.canonicalName
            ownerOfFieldClassName == SomeDtoWithNestedFields.canonicalName
        }
        verifyAll(findField(fieldsDto, "refId")) {
            fieldType.className == Long.canonicalName
        }
        verifyAll(findField(fieldsDto, "referenceNumber")) {
            fieldType.className == String.canonicalName
        }

        verifyAll(findField(fieldsDto, "otherField")) {
            verifyAll(fieldType) {
                fields.size() == 3
                className == SomeRawDto.canonicalName
                verifyAll(findField(fields, "id")) {
                    fieldType.className == Long.canonicalName
                }
                verifyAll(findField(fields, "name")) {
                    fieldType.className == String.canonicalName
                }
                verifyAll(findField(fields, "surname")) {
                    fieldType.className == String.canonicalName
                }
            }
        }

        verifyAll(findField(fieldsDto, "level2")) {
            verifyAll(fieldType) {
                className == NestedObject2L.canonicalName
                fields.size() == 3

                extendsFromModels == null

                verifyAll(findField(fields, "serialNumber")) {
                    fieldType.className == Long.canonicalName
                    ownerOfFieldClassName == SuperNestedObject.canonicalName
                }

                verifyAll(findField(fields, "id")) {
                    fieldType.className == String.canonicalName
                    ownerOfFieldClassName == NestedObject2L.canonicalName
                }
                verifyAll(findField(fields, "level3")) {
                    ownerOfFieldClassName == NestedObject2L.canonicalName
                    verifyAll(fieldType) {
                        className == NestedObject3L.canonicalName
                        verifyAll(findField(fields, "realUuid")) {
                            fieldType.className == UUID.canonicalName
                        }
                    }
                }
            }
        }

        verifyAll(findField(fieldsDto, "level22")) {
            verifyAll(fieldType) {
                className == NestedObject2L.canonicalName
            }
        }
    }

    private static FieldMetaModelDto findField(List<FieldMetaModelDto> fields, String fieldName) {
        fields.find {
            it.fieldName == fieldName
        }
    }
}

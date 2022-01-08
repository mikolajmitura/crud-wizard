package pl.jalokim.crudwizard.core.metamodels

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelWithParents
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import spock.lang.Specification

class ClassMetaModelTest extends Specification {

    def "return expected field names"() {
        given:
        def allFields = [
            "bankField", "name", "surname", "birthDate", "applicationDateTime", "age", "applicationDateTimeAsNumber",
            "personData", "addresses", "hobbies", "contactData", "someNumbersByEnums", "lastContact", "lastText", "numberAsText",
            "someUnique", "someOtherObject", "firsParentField", "rootParentField"
        ] as Set
        ClassMetaModel classMetaModel = createClassMetaModelWithParents()

        when:
        def result = classMetaModel.getFieldNames()

        then:
        result == allFields

        and:
        classMetaModel.getFields()
            .add(createValidFieldMetaModel("newField", Long))

        when:
        result = classMetaModel.getFieldNames()

        then:
        result == allFields

        and:
        classMetaModel.refresh()
        when:
        result = classMetaModel.getFieldNames()

        then:
        result != allFields
        result == allFields + "newField" as Set
    }

    def "return expected types of all fields"() {
        given:
        ClassMetaModel classMetaModel = createClassMetaModelWithParents()

        when:
        def foundFields = classMetaModel.fetchAllFields()

        then:
        foundFields.size() == 19
        assertFieldNameAndType(foundFields, "applicationDateTime", Long)
        assertFieldNameAndType(foundFields, "age", Period)
        assertFieldNameAndType(foundFields, "someUnique", String)
        assertFieldNameAndName(foundFields, "someOtherObject", "some-Other-Object")
        assertFieldNameAndType(foundFields, "lastContact", LocalDateTime)
        assertFieldNameAndType(foundFields, "firsParentField", String)
        assertFieldNameAndType(foundFields, "rootParentField", LocalDateTime)
        assertFieldNameAndType(foundFields, "bankField", String)
        assertFieldNameAndType(foundFields, "name", String)
        assertFieldNameAndType(foundFields, "surname", String)
        assertFieldNameAndType(foundFields, "birthDate", LocalDate)
        assertFieldNameAndType(foundFields, "applicationDateTimeAsNumber", Integer)
        assertFieldNameAndType(foundFields, "personData", SamplePersonDto)
        assertFieldNameAndType(foundFields, "addresses", List)
        assertFieldNameAndType(foundFields, "hobbies", Set)
        assertFieldNameAndType(foundFields, "contactData", Map)
        assertFieldNameAndType(foundFields, "someNumbersByEnums", Map)
        assertFieldNameAndType(foundFields, "lastText", String)
        assertFieldNameAndType(foundFields, "numberAsText", String)

        and:
        classMetaModel.getFields()
            .add(createValidFieldMetaModel("newField", Long))

        when:
        foundFields = classMetaModel.fetchAllFields()

        then:
        foundFields.size() == 19

        and:
        classMetaModel.refresh()

        when:
        foundFields = classMetaModel.fetchAllFields()

        then:
        foundFields.size() == 20
    }

    def "return expected fields type by name"() {
        given:
        ClassMetaModel classMetaModel = createClassMetaModelWithParents()

        when:
        def applicationDateTimeFieldMeta = classMetaModel.getFieldByName("applicationDateTime")
        def ageFieldMeta = classMetaModel.getFieldByName("age")
        def lastContactFieldMeta = classMetaModel.getFieldByName("lastContact")

        then:
        applicationDateTimeFieldMeta.fieldType.realClass == Long
        ageFieldMeta.fieldType.realClass == Period
        lastContactFieldMeta.fieldType.realClass == LocalDateTime
    }

    def "return expected validator"() {
        given:
        ClassMetaModel classMetaModel = createClassMetaModelWithParents()

        when:
        def validators = classMetaModel.getValidators()

        then:
        validators as Set == [ValidatorMetaModelSamples.CUSTOM_TEST_VALIDATOR_METAMODEL, ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL] as Set
    }

    private boolean assertFieldNameAndType(List<FieldMetaModel> foundFields, String fieldName, Class<?> expectedFieldType) {
        verifyAll(foundFields.find {
            it.fieldName == fieldName
        }) {
            fieldType.getRealClass() == expectedFieldType
        }
        return true
    }

    private boolean assertFieldNameAndName(List<FieldMetaModel> foundFields, String fieldName, String expectedName) {
        verifyAll(foundFields.find {
            it.fieldName == fieldName
        }) {
            fieldType.getRealClass() == null
            fieldType.getName() == expectedName
        }
        return true
    }
}

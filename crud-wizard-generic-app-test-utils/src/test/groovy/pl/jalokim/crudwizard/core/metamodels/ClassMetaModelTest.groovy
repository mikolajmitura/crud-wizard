package pl.jalokim.crudwizard.core.metamodels

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelWithParents
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelWithParentsInvalid
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder

import java.time.LocalDate
import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class ClassMetaModelTest extends UnitTestSpec {

    def "return expected field names"() {
        given:
        def allFields = [
            "bankField", "name", "surname", "birthDate", "applicationDateTime", "age", "applicationDateTimeAsNumber",
            "personData", "addresses", "hobbies", "contactData", "someNumbersByEnums", "lastContact", "lastText", "numberAsText",
            "someUnique", "someOtherObject", "firsParentField", "rootParentField", "someNumber"
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
        foundFields.size() == 20
        assertFieldNameAndType(foundFields, "applicationDateTime", LocalDateTime)
        assertFieldNameAndType(foundFields, "age", Integer)
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
        foundFields.size() == 20

        and:
        classMetaModel.refresh()

        when:
        foundFields = classMetaModel.fetchAllFields()

        then:
        foundFields.size() == 21
    }

    def "return expected fields type by name"() {
        given:
        ClassMetaModel classMetaModel = createClassMetaModelWithParents()

        when:
        def applicationDateTimeFieldMeta = classMetaModel.getFieldByName("applicationDateTime")
        def ageFieldMeta = classMetaModel.getFieldByName("age")
        def lastContactFieldMeta = classMetaModel.getFieldByName("lastContact")

        then:
        applicationDateTimeFieldMeta.fieldType.realClass == LocalDateTime
        ageFieldMeta.fieldType.realClass == Integer
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

    @Unroll
    def "is subType of or not"() {
        when:
        def result = subType.isSubTypeOf(someParent)

        then:
        result == isSubType

        where:
        subType                               | someParent                                                      | isSubType
        createClassMetaModelFromClass(Double) | createClassMetaModelFromClass(Number)                           | true
        createClassMetaModelFromClass(Number) | createClassMetaModelFromClass(Double)                           | false

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        createClassMetaModelFromClass(String),
                    ])
                    .build()
            ])
            .build()                          | createClassMetaModelFromClass(String)                           | true

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        createClassMetaModelFromClass(String),
                    ])
                    .build()
            ])
            .build()                          | createClassMetaModelFromClass(Number)                           | true

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        createClassMetaModelFromClass(String),
                    ])
                    .build()
            ])
            .build()                          | createClassMetaModelFromClass(Double)                           | true

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        createClassMetaModelFromClass(String),
                    ])
                    .build()
            ])
            .build()                          | ClassMetaModel.builder().name("someString").build()             | true

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        createClassMetaModelFromClass(String),
                    ])
                    .build()
            ])
            .build()                          | ClassMetaModel.builder().name("childOfDoubleAndString").build() | true

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        createClassMetaModelFromClass(String),
                    ])
                    .build()
            ])
            .build()                          | ClassMetaModel.builder().name("otherName").build()              | false

        ClassMetaModel.builder()
            .name("childOfDoubleAndString")
            .extendsFromModels([
                createClassMetaModelFromClass(Double),
                ClassMetaModel.builder()
                    .name("someString")
                    .extendsFromModels([
                        ClassMetaModel.builder()
                            .name("someOtherString")
                            .build()
                    ])
                    .build()
            ])
            .build()                          | ClassMetaModel.builder().name("someOtherString").build()        | true
    }

    @Unroll
    def "return expected toString on ClassMetaModel"() {
        when:
        def result = classMetaModel.toString()

        then:
        result == expectedText

        where:
        expectedText                                        | classMetaModel
        "ClassMetaModel(name=givenName)"                    | ClassMetaModel.builder().name("givenName").build()
        "ClassMetaModel(id=12)"                             | ClassMetaModel.builder().id(12).build()
        "ClassMetaModel(id=12, name=givenName)"             | ClassMetaModel.builder().id(12).name("givenName").build()
        "ClassMetaModel(id=12, realClass=java.lang.String)" | ClassMetaModel.builder().id(12).realClass(String).build()
    }

    @Unroll
    def "return of expected value of getJavaGenericTypeInfo"() {
        given:

        when:
        def result = classMetaModel.getJavaGenericTypeInfo()

        then:
        result == expectedJavaGenericTypeInfo

        where:
        classMetaModel                                     | expectedJavaGenericTypeInfo
        createClassMetaModelFromClass(String)              | "java.lang.String"
        createClassMetaModelFromClass(CollectionElement[]) | "pl.jalokim.crudwizard.genericapp.mapper.conversion.CollectionElement[]"
        ClassMetaModel.builder()
            .name("someModel")
            .build()                                       | "java.util.Map<java.lang.String, java.lang.Object>"
    }

    def "invalid inheritance found"() {
        given:
        def metamodel = createClassMetaModelWithParentsInvalid()

        when:
        metamodel.fetchAllFields()

        then:
        TechnicalException ex = thrown()
        ex.message == translatePlaceholder("ClassMetaModel.invalid.field.override", "someNumber",
            Number.canonicalName, "first-parent", String.canonicalName, "modelWithParents")
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

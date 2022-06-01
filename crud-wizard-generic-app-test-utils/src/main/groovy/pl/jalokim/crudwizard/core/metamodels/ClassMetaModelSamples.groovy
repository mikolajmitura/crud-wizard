package pl.jalokim.crudwizard.core.metamodels

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.objectToRawJson
import static pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel.ENUM_VALUES_PREFIX

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import pl.jalokim.crudwizard.core.datastorage.ExampleEnum
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.DepartmentDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DefaultDataStorageQueryProvider

class ClassMetaModelSamples {

    static FieldMetaModel createValidFieldMetaModel(String fieldName, Class<?> fieldType, List<ValidatorMetaModel> validators = []) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelFromClass(fieldType, []))
            .validators(validators)
            .build()
    }

    static FieldMetaModel createValidFieldMetaModel(String fieldName, ClassMetaModel fieldType,
        List<ValidatorMetaModel> validators = [], ClassMetaModel ownerOfField = null) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(fieldType)
            .validators(validators)
            .ownerOfField(ownerOfField)
            .build()
    }

    static FieldMetaModel createValidFieldMetaModel(String fieldName, ClassMetaModel fieldType, Map<String, Object> additionalProperties) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(fieldType)
            .additionalProperties(additionalProperties.collect {
                AdditionalPropertyMetaModel.builder()
                    .name(it.key)
                    .valueAsObject(it.value)
                    .rawJson(objectToRawJson(it.value))
                    .build()
            })
            .build()
    }

    static FieldMetaModel createValidFieldMetaModel(String fieldName, Class<?> fieldType, Map<String, Object> additionalProperties) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelFromClass(fieldType))
            .additionalProperties(additionalProperties.collect {
                AdditionalPropertyMetaModel.builder()
                    .name(it.key)
                    .valueAsObject(it.value)
                    .rawJson(objectToRawJson(it.value))
                    .build()
            })
            .build()
    }

    static ClassMetaModel createClassMetaModelFromClass(Class<?> metaModelClass, List<ValidatorMetaModel> validators = []) {
        ClassMetaModel.builder()
            .className(metaModelClass.canonicalName)
            .realClass(metaModelClass)
            .validators(validators)
            .build()
    }

    static ClassMetaModel createValidEnumMetaModel(String name, String... enumValues) {
        ClassMetaModel enumMetaModel = ClassMetaModel.builder()
            .name(name)
            .build()
            .addProperty(ENUM_VALUES_PREFIX, enumValues)
        enumMetaModel.setEnumClassMetaModel(new EnumClassMetaModel(enumMetaModel))
        enumMetaModel
    }

    static ClassMetaModel createSomePersonClassMetaModel() {
        ClassMetaModel.builder()
            .name("somePersonApplication")
            .fields([
                createValidFieldMetaModel("bankField", String),
                createValidFieldMetaModel("name",
                    createClassMetaModelFromClass(String, [ValidatorMetaModelSamples.SIZE_3_20_VALIDATOR_METAMODEL]),
                    [ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL]),
                createValidFieldMetaModel("surname",
                    createClassMetaModelFromClass(String, [ValidatorMetaModelSamples.SIZE_2_30_VALIDATOR_METAMODEL]),
                    [ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL]),
                createValidFieldMetaModel("birthDate", LocalDate),
                createValidFieldMetaModel("applicationDateTime", LocalDateTime),
                createValidFieldMetaModel("age", Integer),
                createValidFieldMetaModel("applicationDateTimeAsNumber", Integer),
                createValidFieldMetaModel("personData", SamplePersonDto),
                createValidFieldMetaModel("addresses", createClassMetaModelFromClass(List).toBuilder()
                    .genericTypes([
                        ClassMetaModel.builder()
                            .name("address")
                            .fields([
                                createValidFieldMetaModel("street", String),
                                createValidFieldMetaModel("houseNr", String, [ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL]),
                                createValidFieldMetaModel("someEnum", ExampleEnum),
                                createValidFieldMetaModel("customEnum", createValidEnumMetaModel("customEnumType", "ENUM1", "ENUM2"))
                            ])
                            .build()
                    ])
                    .build(), [ValidatorMetaModelSamples.SIZE_1_MAX_VALIDATOR_METAMODEL]),
                createValidFieldMetaModel("hobbies", createClassMetaModelFromClass(Set).toBuilder()
                    .genericTypes([createClassMetaModelFromClass(String)])
                    .build()),
                createValidFieldMetaModel("contactData", createClassMetaModelFromClass(Map).toBuilder()
                    .genericTypes([
                        createClassMetaModelFromClass(String),
                        createClassMetaModelFromClass(String)
                    ])
                    .build()),
                createValidFieldMetaModel("someNumbersByEnums", createClassMetaModelFromClass(Map).toBuilder()
                    .genericTypes([
                        createClassMetaModelFromClass(ExampleEnum),
                        createClassMetaModelFromClass(Integer)
                    ])
                    .build())
            ])
            .validators([ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL])
            .build()
    }

    static ClassMetaModel createQueryArgumentsMetaModel() {
        ClassMetaModel.builder()
            .name("somePersonApplication-queryParams")
            .fields([
                createValidFieldMetaModel("lastContact", LocalDate),
                createValidFieldMetaModel("lastText", String),
                createValidFieldMetaModel("numberAsText", String)])
            .build()
    }

    static ClassMetaModel createPathParamsClassMetaModel() {
        ClassMetaModel.builder()
            .name("pathParamsMeta")
            .fields([
                createValidFieldMetaModel("usersIdVar", String),
                createValidFieldMetaModel("ordersIdVar", Long)
            ])
            .build()
    }

    static ClassMetaModel createHttpQueryParamsForPerson() {
        ClassMetaModel.builder()
            .name("person-queryParams")
            .fields([
                createValidFieldMetaModel("name", String, Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "LIKE")),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("age", Integer, Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "GREATER_THAN")),
                createValidFieldMetaModel("otherNumber", Integer, Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "LOWER_THAN")),
                createValidFieldMetaModel("someTexts", createClassMetaModelFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelFromClass(String)]).build(), Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "IN")),
                createValidFieldMetaModel("someNumbers", createClassMetaModelFromClass(List).toBuilder()
                    .genericTypes([createClassMetaModelFromClass(Integer)]).build(), Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "IN")),
                createValidFieldMetaModel("pesel", Integer, Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "IS_NULL")),
                createValidFieldMetaModel("nationality", Integer, Map.of(DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "IS_NOT_NULL")),
                createValidFieldMetaModel("documentType", String,
                    Map.of(
                        DefaultDataStorageQueryProvider.EXPRESSION_RIGHT_PATH, "rightPath.otherValue",
                        DefaultDataStorageQueryProvider.EXPRESSION_LEFT_PATH, "document.type",
                        DefaultDataStorageQueryProvider.EXPRESSION_TYPE, "EQUALS")),
                createValidFieldMetaModel("sort", String)]
            )
            .build()
    }

    static ClassMetaModel createClassMetaModelWithParents() {
        ClassMetaModel.builder()
            .name("modelWithParents")
            .fields([
                createValidFieldMetaModel("applicationDateTime", Long),
                createValidFieldMetaModel("age", Period),
                createValidFieldMetaModel("someUnique", String),
                createValidFieldMetaModel("someOtherObject", ClassMetaModel.builder()
                    .name("some-Other-Object")
                    .fields([
                        createValidFieldMetaModel("someField1", String),
                        createValidFieldMetaModel("someField2", String)
                    ])
                    .validators([ValidatorMetaModelSamples.SOME_OTHER_OBJECT_VALIDATOR_METAMODEL])
                    .build()),
            ])
            .extendsFromModels([
                ClassMetaModel.builder()
                    .name("first-parent")
                    .fields([
                        createValidFieldMetaModel("lastContact", LocalDateTime),
                        createValidFieldMetaModel("firsParentField", String),
                    ])
                    .extendsFromModels([
                        ClassMetaModel.builder()
                            .name("root-parent")
                            .fields([
                                createValidFieldMetaModel("rootParentField", LocalDateTime)
                            ])
                            .build()
                    ])
                    .build(),
                createSomePersonClassMetaModel(), createQueryArgumentsMetaModel()])
            .validators([ValidatorMetaModelSamples.CUSTOM_TEST_VALIDATOR_METAMODEL])
            .build()
    }

    static ClassMetaModel createSimplePersonMetaModel() {
        def classMetamodel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("id", Long),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("fullName", String),
                createValidFieldMetaModel("passportData", createSimpleDocumentMetaModel()),
                createValidFieldMetaModel("fatherData", ExtendedSamplePersonDto)
            ])
            .build()

        classMetamodel.fields.each {
            it.ownerOfField = classMetamodel
        }

        classMetamodel
    }

    static ClassMetaModel createSimpleDocumentMetaModel() {
        ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("id", Long),
                createValidFieldMetaModel("documentNumber", String),
                createValidFieldMetaModel("validTo", LocalDate)
            ])
            .build()
    }

    static ClassMetaModel createEmployeePersonMeta() {
        ClassMetaModel.builder()
            .name("employee-person")
            .extendsFromModels([
                createSimplePersonMetaModel(),
                createClassMetaModelFromClass(DepartmentDto)])
            .fields([
                createValidFieldMetaModel("employeeId", Long),
                createValidFieldMetaModel("fullName", Map),
                createValidFieldMetaModel("boss", createSimplePersonMetaModel()),
            ])
            .build()
    }

    static ClassMetaModel createClassModelWithGenerics(Class<?> rawClass, Class<?>... genericTypes) {
        ClassMetaModel.builder()
            .realClass(rawClass)
            .genericTypes(genericTypes.collect { createClassMetaModelFromClass(it)})
            .build()
    }

    static ClassMetaModel createClassModelWithGenerics(Class<?> rawClass, ClassMetaModel... genericTypes) {
        ClassMetaModel.builder()
            .realClass(rawClass)
            .genericTypes(genericTypes as List<ClassMetaModel>)
            .build()
    }
}

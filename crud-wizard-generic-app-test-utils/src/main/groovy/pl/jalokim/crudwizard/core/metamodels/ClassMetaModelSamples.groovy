package pl.jalokim.crudwizard.core.metamodels

import static pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel.ENUM_VALUES_PREFIX

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import pl.jalokim.crudwizard.core.datastorage.ExampleEnum
import pl.jalokim.crudwizard.core.sample.SamplePersonDto

class ClassMetaModelSamples {

    static FieldMetaModel createValidFieldMetaModel(String fieldName, Class<?> fieldType, List<ValidatorMetaModel> validators = []) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelFromClass(fieldType))
            .validators(validators)
            .build()
    }

    static FieldMetaModel createValidFieldMetaModel(String fieldName, ClassMetaModel fieldType, List<ValidatorMetaModel> validators = []) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(fieldType)
            .validators(validators)
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

    static ClassMetaModel createRequestBodyClassMetaModel() {
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

    static ClassMetaModel createHttpQueryParamsMetaModel() {
        ClassMetaModel.builder()
            .name("somePersonApplication-queryParams")
            .fields([
                createValidFieldMetaModel("lastContact", LocalDate),
                createValidFieldMetaModel("lastText", String),
                createValidFieldMetaModel("numberAsText", String)])
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
                createRequestBodyClassMetaModel(), createHttpQueryParamsMetaModel()])
            .validators([ValidatorMetaModelSamples.CUSTOM_TEST_VALIDATOR_METAMODEL])
            .build()
    }
}

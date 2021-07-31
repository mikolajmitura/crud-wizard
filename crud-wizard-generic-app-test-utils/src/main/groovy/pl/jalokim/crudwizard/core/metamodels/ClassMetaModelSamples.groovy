package pl.jalokim.crudwizard.core.metamodels

import java.time.LocalDate
import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.datastorage.ExampleEnum
import pl.jalokim.crudwizard.core.sample.SamplePersonDto

class ClassMetaModelSamples {

    static FieldMetaModel createValidFieldMetaModel(String fieldName, Class<?> fieldType) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelFromClass(fieldType))
            .build()
    }

    static FieldMetaModel createValidFieldMetaModel(String fieldName, ClassMetaModel fieldType) {
        FieldMetaModel.builder()
            .fieldName(fieldName)
            .fieldType(fieldType)
            .build()
    }

    static ClassMetaModel createClassMetaModelFromClass(Class<?> metaModelClass) {
        ClassMetaModel.builder()
            .className(metaModelClass.canonicalName)
            .realClass(metaModelClass)
            .build()
    }

    static ClassMetaModel createRequestBodyClassMetaModel() {
        ClassMetaModel.builder()
            .name("somePersonApplication")
            .fields([
                createValidFieldMetaModel("bankField", String),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("birthDate", LocalDate),
                createValidFieldMetaModel("applicationDateTime", LocalDateTime),
                createValidFieldMetaModel("age", Integer),
                createValidFieldMetaModel("applicationDateTime", Integer),
                createValidFieldMetaModel("personData", SamplePersonDto),
                createValidFieldMetaModel("addresses", createClassMetaModelFromClass(ArrayList).toBuilder()
                    .genericTypes([
                        ClassMetaModel.builder()
                            .name("address")
                            .fields([
                                createValidFieldMetaModel("street", String),
                                createValidFieldMetaModel("houseNr", String),
                                createValidFieldMetaModel("someEnum", ExampleEnum),
                            ])
                            .build()
                    ])
                    .build()),
                createValidFieldMetaModel("hobbies", createClassMetaModelFromClass(HashSet).toBuilder()
                    .genericTypes([createClassMetaModelFromClass(String)])
                    .build()),
                createValidFieldMetaModel("contactData", createClassMetaModelFromClass(HashMap).toBuilder()
                    .genericTypes([
                        createClassMetaModelFromClass(String),
                        createClassMetaModelFromClass(String)
                    ])
                    .build()),
                createValidFieldMetaModel("someNumbersByEnums", createClassMetaModelFromClass(HashMap).toBuilder()
                    .genericTypes([
                        createClassMetaModelFromClass(ExampleEnum),
                        createClassMetaModelFromClass(Integer)
                    ])
                    .build())
            ])
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
}

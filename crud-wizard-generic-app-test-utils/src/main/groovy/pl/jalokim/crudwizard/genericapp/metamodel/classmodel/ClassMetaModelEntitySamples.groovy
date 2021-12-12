package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import java.time.LocalDate

class ClassMetaModelEntitySamples {

    static ClassMetaModelEntity personMetaEntity() {
        ClassMetaModelEntity.builder()
            .name("person")
            .fields([
                fieldEntity("id", Long),
                fieldEntity("name", String),
                fieldEntity("surname", String),
                fieldEntity("fullName", String),
                fieldEntity("passportData", documentMetaEntity()),
                fieldEntity("fatherData", ExtendedSamplePersonDto)
            ])
            .build()
    }

    static ClassMetaModelEntity documentMetaEntity() {
        ClassMetaModelEntity.builder()
            .name("document")
            .fields([
                fieldEntity("id", Long),
                fieldEntity("documentNumber", String),
                fieldEntity("validTo", LocalDate)
            ])
            .build()
    }

    static ClassMetaModelEntity employeePersonMetaEntity() {
        ClassMetaModelEntity.builder()
            .name("employee-person")
            .extendsFromModels([personMetaEntity(), classMetaModelEntityFrom(DepartmentDto)])
            .fields([
                fieldEntity("employeeId", Long),
                fieldEntity("fullName", Map),
                fieldEntity("boss", personMetaEntity()),
            ])
            .build()
    }

    static FieldMetaModelEntity fieldEntity(String name, Class<?> fieldType) {
        fieldEntity(name, classMetaModelEntityFrom(fieldType))
    }

    static FieldMetaModelEntity fieldEntity(String name, ClassMetaModelEntity fieldType) {
        FieldMetaModelEntity.builder()
            .fieldName(name)
            .fieldType(fieldType)
            .build()
    }

    static ClassMetaModelEntity classMetaModelEntityFrom(Class<?> rawType) {
        ClassMetaModelEntity.builder()
            .className(rawType.canonicalName)
            .build()
    }
}

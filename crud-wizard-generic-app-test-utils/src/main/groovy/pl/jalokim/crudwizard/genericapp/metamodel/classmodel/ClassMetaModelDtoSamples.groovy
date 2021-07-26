package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

import static pl.jalokim.utils.test.DataFakerHelper.randomText

import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto

class ClassMetaModelDtoSamples {

    static ClassMetaModelDto createValidClassMetaModelDtoWithName() {
        ClassMetaModelDto.builder()
            .name(randomText())
            .fields([createValidFieldMetaModelDto()])
            .build()
    }

    static ClassMetaModelDto createValidClassMetaModelDtoWithClassName() {
        ClassMetaModelDto.builder()
            .className(String.canonicalName)
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoFromClass(Class<?> metaModelClass) {
        ClassMetaModelDto.builder()
            .className(metaModelClass.canonicalName)
            .build()
    }

    static ClassMetaModelDto createEmptyClassMetaModelDto() {
        ClassMetaModelDto.builder()
            .build()
    }

    static FieldMetaModelDto createValidFieldMetaModelDto() {
        FieldMetaModelDto.builder()
            .fieldName(randomText())
            .fieldType(createValidClassMetaModelDtoWithClassName())
            .build()
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel

class ClassMetaModelDtoSamples {

    static ClassMetaModelDto createValidClassMetaModelDto() {
        ClassMetaModelDto.builder()
            .build()
    }

    static ClassMetaModelDto createClassMetaModelDtoFromClass(Class<?> metaModelClass) {
        ClassMetaModelDto.builder()
            .className(metaModelClass.canonicalName)
            .build()
    }
}

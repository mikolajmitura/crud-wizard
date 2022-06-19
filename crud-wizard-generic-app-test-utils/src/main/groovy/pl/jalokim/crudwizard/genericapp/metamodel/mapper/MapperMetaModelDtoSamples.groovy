package pl.jalokim.crudwizard.genericapp.metamodel.mapper

import static MapperType.BEAN_OR_CLASS_NAME

class MapperMetaModelDtoSamples {

    static MapperMetaModelDto createMapperMetaModelDto(Class<?> mapperClass, String mapperMethodName) {
        MapperMetaModelDto.builder()
            .className(mapperClass.canonicalName)
            .methodName(mapperMethodName)
            .mapperType(BEAN_OR_CLASS_NAME)
            .build()
    }
}

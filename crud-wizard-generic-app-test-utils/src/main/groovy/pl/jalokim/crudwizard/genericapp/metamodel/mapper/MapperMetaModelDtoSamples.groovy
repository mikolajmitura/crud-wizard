package pl.jalokim.crudwizard.genericapp.metamodel.mapper

class MapperMetaModelDtoSamples {

    static MapperMetaModelDto createMapperMetaModelDto(Class<?> mapperClass, String mapperMethodName) {
        MapperMetaModelDto.builder()
            .className(mapperClass.canonicalName)
            .methodName(mapperMethodName)
            .build()
    }
}

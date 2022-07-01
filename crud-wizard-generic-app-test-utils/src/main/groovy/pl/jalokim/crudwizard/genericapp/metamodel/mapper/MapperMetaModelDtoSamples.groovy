package pl.jalokim.crudwizard.genericapp.metamodel.mapper

import static MapperType.BEAN_OR_CLASS_NAME

import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto

class MapperMetaModelDtoSamples {

    static MapperMetaModelDto createMapperMetaModelDto(Class<?> mapperClass, String mapperMethodName) {
        MapperMetaModelDto.builder()
            .mapperBeanAndMethod(BeanAndMethodDto.builder()
                .className(mapperClass.canonicalName)
                .methodName(mapperMethodName)
                .build())
            .mapperType(BEAN_OR_CLASS_NAME)
            .build()
    }
}

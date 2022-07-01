package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.utils.test.DataFakerHelper.randomText

import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto

class ServiceMetaModelDtoSamples {

    static ServiceMetaModelDto createValidServiceMetaModelDto() {
        ServiceMetaModelDto.builder()
        .serviceBeanAndMethod(BeanAndMethodDto.builder()
            .className(Object.class.getCanonicalName())
            .beanName(randomText())
            .methodName(randomText())
            .build())
            .build()
    }

    static ServiceMetaModelDto createValidServiceMetaModelDtoAsScript() {
        ServiceMetaModelDto.builder()
            .serviceScript(randomText())
            .build()
    }
}

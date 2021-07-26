package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.utils.test.DataFakerHelper.randomText

class ServiceMetaModelDtoSamples {

    static ServiceMetaModelDto createValidServiceMetaModelDto() {
        ServiceMetaModelDto.builder()
            .className(randomText())
            .beanName(randomText())
            .methodName(randomText())
            .build()
    }

    static ServiceMetaModelDto createValidServiceMetaModelDtoAsScript() {
        ServiceMetaModelDto.builder()
            .serviceScript(randomText())
            .build()
    }
}

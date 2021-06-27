package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.crudwizard.test.utils.random.DataFakerHelper.randomText

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

package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.utils.test.DataFakerHelper.randomText

import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto

class ServiceMetaModelDtoSamples {

    static ServiceMetaModelDto createValidServiceMetaModelDto() {
        ServiceMetaModelDto.builder()
        .serviceBeanAndMethod(BeanAndMethodDto.builder()
            .className(DummyService.class.getCanonicalName())
            .beanName("dummyService")
            .methodName("somePost")
            .build())
            .build()
    }

    static ServiceMetaModelDto createValidServiceMetaModelDtoAsScript() {
        ServiceMetaModelDto.builder()
            .serviceScript(randomText())
            .build()
    }
}

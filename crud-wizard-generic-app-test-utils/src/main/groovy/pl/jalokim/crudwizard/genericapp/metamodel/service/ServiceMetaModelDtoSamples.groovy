package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.utils.test.DataFakerHelper.randomText

import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto

class ServiceMetaModelDtoSamples {

    static ServiceMetaModelDto createValidServiceMetaModelDto() {
        createValidServiceMetaModelDto(DummyService, "somePost")
    }

    static ServiceMetaModelDto createValidServiceMetaModelDto(Class<?> type, String methodName, String beanName = null) {
        ServiceMetaModelDto.builder()
            .serviceBeanAndMethod(BeanAndMethodDto.builder()
                .className(type.getCanonicalName())
                .beanName(beanName)
                .methodName(methodName)
                .build())
            .build()
    }

    static ServiceMetaModelDto createValidServiceMetaModelDtoAsScript() {
        ServiceMetaModelDto.builder()
            .serviceScript(randomText())
            .build()
    }
}

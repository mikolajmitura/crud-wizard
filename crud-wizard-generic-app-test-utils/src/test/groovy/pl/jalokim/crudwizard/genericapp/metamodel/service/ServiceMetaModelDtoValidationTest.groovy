package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDtoAsScript
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class ServiceMetaModelDtoValidationTest extends UnitTestSpec {

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter()

    @Unroll
    def "should return expected messages for default context of EndpointMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(serviceMetaModelDtoSamples)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        serviceMetaModelDtoSamples               | expectedErrors
        createValidServiceMetaModelDto()         | []

        createValidServiceMetaModelDto().toBuilder()
            .serviceBeanAndMethod(BeanAndMethodDto.builder()
                .className("not.exist.class")
                .build())
            .build()                             | [
            errorEntry("serviceBeanAndMethod.methodName", notNullMessage())
        ]
        createValidServiceMetaModelDtoAsScript() | []

        createValidServiceMetaModelDtoAsScript()
            .toBuilder()
            .serviceBeanAndMethod(BeanAndMethodDto.builder()
                .className(Object.class.getCanonicalName())
                .beanName(randomText())
                .methodName(randomText())
                .build())
            .build()                             | [
            errorEntry("serviceBeanAndMethod", fieldShouldWhenOtherMessage(NULL, [], "serviceScript", NOT_NULL, [])),
            errorEntry("serviceScript", fieldShouldWhenOtherMessage(NULL, [], "serviceBeanAndMethod", NOT_NULL, []))
        ]
    }
}

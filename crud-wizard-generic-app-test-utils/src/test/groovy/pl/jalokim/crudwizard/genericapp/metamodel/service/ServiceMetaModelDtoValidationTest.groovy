package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDtoAsScript
import static pl.jalokim.crudwizard.test.utils.random.DataFakerHelper.randomText
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage;
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter;

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter;
import spock.lang.Unroll;

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
        createValidServiceMetaModelDtoAsScript() | []
        createValidServiceMetaModelDtoAsScript()
            .toBuilder()
            .className(randomText())
            .beanName(randomText())
            .methodName(randomText())
            .build()                             | [
            errorEntry("className", fieldShouldWhenOtherMessage(NULL, [], "serviceScript", NOT_NULL, [])),
            errorEntry("beanName", fieldShouldWhenOtherMessage(NULL, [], "serviceScript", NOT_NULL, [])),
            errorEntry("methodName", fieldShouldWhenOtherMessage(NULL, [], "serviceScript", NOT_NULL, [])),
            errorEntry("serviceScript", fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, [])),
            errorEntry("serviceScript", fieldShouldWhenOtherMessage(NULL, [], "beanName", NOT_NULL, [])),
            errorEntry("serviceScript", fieldShouldWhenOtherMessage(NULL, [], "methodName", NOT_NULL, []))
        ]
    }
}

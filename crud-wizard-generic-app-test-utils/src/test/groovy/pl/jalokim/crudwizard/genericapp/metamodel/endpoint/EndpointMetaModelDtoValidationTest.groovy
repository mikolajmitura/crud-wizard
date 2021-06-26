package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter

import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class EndpointMetaModelDtoValidationTest extends UnitTestSpec {

    private ValidatorWithConverter validatorWithConverter = createValidatorWithConverter()

    // TODO #1 as first to implements
    @Unroll
    def "should return expected messages for default context of EndpointMetaModelDto"() {
        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(endpointMetaModelDto)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        endpointMetaModelDto                  | expectedErrors
        createValidPostEndpointMetaModelDto() | []
        createValidPostEndpointMetaModelDto() | []
    }

    private static EndpointMetaModelDto emptyEndpointMetaModelDto() {
        EndpointMetaModelDto.builder().build()
    }

}

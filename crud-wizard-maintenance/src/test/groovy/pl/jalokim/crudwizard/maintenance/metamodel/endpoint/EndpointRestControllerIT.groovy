package pl.jalokim.crudwizard.maintenance.metamodel.endpoint

import static org.hamcrest.MatcherAssert.assertThat
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.core.validation.javax.ClassExists
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelRepository
import pl.jalokim.crudwizard.maintenance.MaintenanceBaseIntegrationController
import pl.jalokim.crudwizard.test.utils.rest.ErrorResponseMatcher

/*
    Test for only just check that endpoints were invoked like create, delete update with validation results etc
    real tests should be in EndpointMetaModelServiceIT
 */

class EndpointRestControllerIT extends MaintenanceBaseIntegrationController {

    @Autowired
    private OperationsOnEndpointRestController endpointRestController

    @Autowired
    private EndpointMetaModelRepository endpointMetaModelRepository

    def "should save new endpoint to db"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto()

        when:
        long createdId = endpointRestController.create(createEndpointMetaModelDto)

        then:
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.findExactlyOneById(createdId)
            assert endpointEntity != null
        }
    }

    def "should return validation errors during save empty endpoint"() {
        given:
        def createEndpointMetaModelDto = emptyEndpointMetaModelDto()

        when:
        def jsonErrorsResponse = endpointRestController.notSuccessCreate(createEndpointMetaModelDto)

        then:
        assertThat(jsonErrorsResponse, ErrorResponseMatcher.hasError('baseUrl', notNullMessage()))
    }

    def "should return errors only from first validation phase"() {
        given:
        def createInvalidPayload = EndpointMetaModelDto.builder()
            .payloadMetamodel(ClassMetaModelDto.builder()
                .classMetaModelDtoType(null)
                .name("some-name")
                .className("com.test.NotExistClass")
                .build())
            .build()

        when:
        def errorResponse = endpointRestController.notSuccessCreateGetErrors(createInvalidPayload)

        then:
        assertValidationResults(errorResponse.getErrors(), [
            errorEntry("payloadMetamodel.classMetaModelDtoType", notNullMessage()),
            errorEntry("payloadMetamodel.className",
                messageForValidator(ClassExists, "expectedOfType", Object.canonicalName))
        ])
    }
}

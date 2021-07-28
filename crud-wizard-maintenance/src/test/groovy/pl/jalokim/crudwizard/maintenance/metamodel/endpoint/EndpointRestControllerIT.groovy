package pl.jalokim.crudwizard.maintenance.metamodel.endpoint

import static org.hamcrest.MatcherAssert.assertThat
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelRepository
import pl.jalokim.crudwizard.maintenance.MaintenanceBaseIntegrationController
import pl.jalokim.crudwizard.test.utils.rest.ErrorResponseMatcher

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

    // TODO only just check that endpoints were invoked create, delete update etc real test should be in EndpointMetaModelServiceIT
}

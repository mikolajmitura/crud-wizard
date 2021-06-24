package pl.jalokim.crudwizard.maintenance.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelRepository
import pl.jalokim.crudwizard.maintenance.MaintenanceBaseIntegrationController

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
            verifyAll(endpointEntity) {
                baseUrl == createEndpointMetaModelDto.baseUrl
                httpMethod == createEndpointMetaModelDto.httpMethod
                operationName == createEndpointMetaModelDto.operationName
                apiTag.name == createEndpointMetaModelDto.apiTag.name
            }
        }
    }

    // TODO only just check that endpoints were invoked
}

package pl.jalokim.crudwizard.maintenance.metamodel.endpoint

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.core.metamodels.ApiTagMetamodel
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelRepository
import pl.jalokim.crudwizard.maintenance.MaintenanceBaseIntegrationController

class EndpointRestControllerIT extends MaintenanceBaseIntegrationController {

    @Autowired
    private OperationsOnEndpointRestController endpointRestController

    @Autowired
    private EndpointMetaModelRepository endpointMetaModelRepository
    // TODO NOW test for simple case... simple new model with one field as another metamodel...
    //  default data storage, default mapper, default service, data storage model as endpoint model

    def "should save new endpoint to db"() {
        given:
        EndpointMetaModel createEndpointMetaModelDto = createCreateEndpointMetaModelDto()

        when:
        long createdId = endpointRestController.create(createEndpointMetaModelDto)

        then:
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.findExactlyOneById(createdId)

        }
    }

    EndpointMetaModel createCreateEndpointMetaModelDto() {
        EndpointMetaModel.builder()
            .baseUrl("users")
            .apiTag(ApiTagMetamodel.builder()
                .name("users")
                .build())
            .httpMethod(HttpMethod.POST)
            .operationName("createUser")
            .build()
    }
}

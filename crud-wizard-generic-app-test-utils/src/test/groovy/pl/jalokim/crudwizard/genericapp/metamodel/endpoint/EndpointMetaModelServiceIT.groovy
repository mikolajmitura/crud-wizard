package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification

class EndpointMetaModelServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private EndpointMetaModelRepository endpointMetaModelRepository

    def "should save simple POST new endpoint with default mapper, service, data storage"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto()

        when:
        def createdId = endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.findExactlyOneById(createdId)
            verifyAll(endpointEntity) {
                verifyAll(apiTag) {
                    id != null
                    name == createEndpointMetaModelDto.apiTag.name
                }
                baseUrl == createEndpointMetaModelDto.baseUrl
                httpMethod == createEndpointMetaModelDto.httpMethod
                operationName == createEndpointMetaModelDto.operationName
                def inputPayloadMetamodel = createEndpointMetaModelDto.payloadMetamodel
                verifyAll(payloadMetamodel) {
                    name == inputPayloadMetamodel.name
                    fields.size() == 1
                    verifyAll(fields[0]) {
                        fieldName == inputPayloadMetamodel.fields[0].fieldName
                        verifyAll(fieldType) {
                            className == inputPayloadMetamodel.fields[0].fieldType.className
                        }
                    }
                }
                verifyAll(responseMetaModel) {
                    verifyAll(classMetaModel) {
                        name == null
                        className == createEndpointMetaModelDto.responseMetaModel.classMetaModel.className
                    }
                    successHttpCode == createEndpointMetaModelDto.responseMetaModel.successHttpCode
                }
            }
        }
    }
    // TODO save all fields during create endpoint metamodels, queryArguments, responseMetaModel with not raw java class but with class metadata,
    //  classMetaModelInDataStorage other than payload
    // TODO new endpoint with already existed metamodels
    // TODO save with new service
    // TODO save with new mapper
    // TODO save with not default data storage
}

package pl.jalokim.crudwizard.genericapp.metamodel.context

import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperBean
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagSamples
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService
import pl.jalokim.crudwizard.genericapp.provider.DefaultBeansConfigService
import pl.jalokim.crudwizard.genericapp.service.GenericServiceBean

class MetaModelContextServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private ApiTagSamples apiTagSamples

    @Autowired
    private MetaModelContextService metaModelContextService

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private DataStorageMetaModelService dataStorageMetaModelService

    @Autowired
    private ApiTagService apiTagService

    @Autowired
    private MapperMetaModelService mapperMetaModelService

    @Autowired
    private ServiceMetaModelService serviceMetaModelService

    @Autowired
    private DefaultBeansConfigService defaultBeansService

    @Autowired
    private InMemoryDataStorage inMemoryDataStorage

    @Autowired
    private GenericMapperBean genericMapperBean

    @Autowired
    private GenericServiceBean genericServiceBean

    /*
    test for create new endpoint which doesn't use currently created meta model. It creates all of them.
     */

    def "should load all meta models as expected for one endpoint with default mapper, default service, default data storage"() {
        given:
        def firstApiTag = apiTagSamples.saveNewApiTag()
        def secondApiTag = apiTagSamples.saveNewApiTag()
        def endpointMetaModelDto = createValidPostEndpointMetaModelDto()
        def endpointId = endpointMetaModelService.createNewEndpoint(endpointMetaModelDto)

        when:
        metaModelContextService.reloadAll()
        def reloadedContext = metaModelContextService.getMetaModelContext()

        then:
        verifyAll(reloadedContext) {
            dataStorages.modelsById == dataStorageMetaModelService.findAllMetaModels()
                .collectEntries {
                    [it.id, it]
                }
            verifyAll(defaultDataStorageMetaModel) {
                id == defaultBeansService.getDefaultDataStorageId()
                name == InMemoryDataStorage.DEFAULT_DS_NAME
                dataStorage == inMemoryDataStorage
            }

            apiTags.modelsById == apiTagService.findAll()
                .collectEntries {
                    [it.id, it]
                }
            apiTags.modelsById.values()*.name as Set == [firstApiTag, secondApiTag, endpointMetaModelDto.apiTag]*.name as Set

            validatorMetaModels.modelsById.isEmpty()

            def payloadMetaModelInstance = classMetaModels.modelsById.values().find {
                it.name == endpointMetaModelDto.payloadMetamodel.name
            }

            verifyAll(payloadMetaModelInstance) {
                realClass == null
                className == null
                fields.size() == 1
                verifyAll(fields[0]) {
                    fieldName == endpointMetaModelDto.payloadMetamodel.fields[0].fieldName
                    fieldType.realClass == String
                    fieldType.className == String.canonicalName
                }
            }

            verifyAll(classMetaModels.modelsById.values().find {
                it.className == String.canonicalName
            }) {
                realClass == String
            }

            def apiResponseMetaModel = classMetaModels.modelsById.values().find {
                it.className == Long.canonicalName
            }
            verifyAll(apiResponseMetaModel) {
                realClass == Long
            }

            mapperMetaModels.modelsById.values()*.className as Set == mapperMetaModelService.findAllMetaModels()*.className as Set
            verifyAll(defaultMapperMetaModel) {
                id == defaultBeansService.getDefaultGenericMapperId()
                mapperInstance == genericMapperBean
                className == GenericMapperBean.canonicalName
                beanName == "genericMapperBean"
                methodName == "mapToTarget"
                methodMetaModel.name == "mapToTarget"
                mappingDirection == null
                mapperScript == null
            }

            serviceMetaModels.modelsById.values()*.className as Set == serviceMetaModelService.findAllMetaModels()*.className as Set
            verifyAll(defaultServiceMetaModel) {
                id == defaultBeansService.getDefaultGenericServiceId()
                serviceInstance == genericServiceBean
                className == GenericServiceBean.canonicalName
                beanName == "genericServiceBean"
                methodName == "saveOrReadFromDataStorages"
                methodMetaModel.name == "saveOrReadFromDataStorages"
                serviceScript == null
            }

            defaultDataStorageConnectorMetaModels*.id as Set == defaultBeansService.getDefaultDataStorageConnectorsId() as Set
            verifyAll(defaultDataStorageConnectorMetaModels[0]) {
                dataStorageMetaModel == defaultDataStorageMetaModel
                mapperMetaModel == defaultMapperMetaModel
                classMetaModelInDataStorage == null
            }

            verifyAll(endpointMetaModels.modelsById.get(endpointId)) {
                id == endpointId
                apiTag.id != null
                apiTag.name == endpointMetaModelDto.apiTag.name
                urlMetamodel.rawUrl == endpointMetaModelDto.baseUrl
                httpMethod == endpointMetaModelDto.httpMethod
                operationName == endpointMetaModelDto.operationName
                payloadMetamodel == payloadMetaModelInstance
                serviceMetaModel == defaultServiceMetaModel
                verifyAll(responseMetaModel) {
                    id != null
                    classMetaModel == apiResponseMetaModel
                    successHttpCode == endpointMetaModelDto.responseMetaModel.successHttpCode
                    dataStorageConnectors == defaultDataStorageConnectorMetaModels
                }
            }

            verifyAll(endpointMetaModelContextNode.nextNodesByPath[endpointMetaModelDto.baseUrl]) {
                urlPart.originalValue == endpointMetaModelDto.baseUrl
                endpointsByHttpMethod[endpointMetaModelDto.httpMethod] == endpointMetaModels.modelsById.get(endpointId)
            }
        }
    }

    // TODO #2 to implement load context with custom service, mapper, data storage connectors, should verify only them not all modelsById fields.ApiTagSamples
}

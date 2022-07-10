package pl.jalokim.crudwizard.genericapp.provider

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.mapper.DefaultGenericMapper
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelRepository
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService

class DefaultBeansConfigServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private DefaultBeansConfigService defaultBeansConfigService

    @Autowired
    private DataStorageMetaModelRepository dataStorageMetaModelRepository

    @Autowired
    private ServiceMetaModelRepository serviceMetaModelRepository

    @Autowired
    private MapperMetaModelEntityRepository mapperMetaModelEntityRepository

    @Autowired
    private DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository

    def "should save defaults generic beans meta models and can get default id of them"() {
        when:
        defaultBeansConfigService.saveAllDefaultMetaModels()
        def defaultDataStorageId = defaultBeansConfigService.getDefaultDataStorageId()
        def defaultGenericMapperId = defaultBeansConfigService.getDefaultGenericMapperId()
        def genericServiceMetaModelId = defaultBeansConfigService.getDefaultGenericServiceId()
        def defaultDataStorageConnectorsId = defaultBeansConfigService.getDefaultDataStorageConnectorsId()

        then:
        defaultDataStorageConnectorsId.size() == 1
        inTransaction {
            verifyAll(dataStorageMetaModelRepository.getOne(defaultDataStorageId)) {
                name == InMemoryDataStorage.DEFAULT_DS_NAME
                className == InMemoryDataStorage.canonicalName
            }

            verifyAll(mapperMetaModelEntityRepository.getOne(defaultGenericMapperId)) {
                verifyAll(mapperBeanAndMethod) {
                    className == DefaultGenericMapper.canonicalName
                    beanName == "defaultGenericMapper"
                    methodName == "mapToTarget"
                }
            }

            verifyAll(serviceMetaModelRepository.getOne(genericServiceMetaModelId)) {
                verifyAll(serviceBeanAndMethod) {
                    className == DefaultGenericService.canonicalName
                    beanName == "defaultGenericService"
                    methodName == "saveOrReadFromDataStorages"
                }
                serviceScript == null
            }

            verifyAll(dataStorageConnectorMetaModelRepository.getOne(defaultDataStorageConnectorsId[0])) {
                dataStorageMetaModel.id == defaultDataStorageId
                mapperMetaModelForQuery.id == defaultGenericMapperId
                mapperMetaModelForPersist.id == defaultGenericMapperId
                classMetaModelInDataStorage == null
            }
        }
    }
}

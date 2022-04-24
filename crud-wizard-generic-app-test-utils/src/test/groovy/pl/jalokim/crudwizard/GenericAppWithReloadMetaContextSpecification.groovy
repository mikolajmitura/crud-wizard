package pl.jalokim.crudwizard

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import pl.jalokim.crudwizard.core.datastorage.DataStorage
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEvent
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageInstances

class GenericAppWithReloadMetaContextSpecification extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher

    @Autowired
    private MetaModelContextService metaModelContextService

    @Autowired
    private DataStorageInstances dataStorageInstances

    @Autowired
    private List<DataStorage> defaultDataStorages

    def setup() {
        metaModelContextService.getMetaModelContext()
            .getDataStorages().fetchAll().each {
            if (it.getDataStorage() instanceof InMemoryDataStorage) {
                ((InMemoryDataStorage) it.getDataStorage()).clear()
            }
        }

        dataStorageInstances.dataStorages.clear()
        dataStorageInstances.dataStorages.addAll(defaultDataStorages)

        reloadMetaModelsContext()
    }

    protected reloadMetaModelsContext() {
        applicationEventPublisher.publishEvent(new MetaModelContextRefreshEvent("reload for test", timeProvider.getCurrentOffsetDateTime()))
    }
}

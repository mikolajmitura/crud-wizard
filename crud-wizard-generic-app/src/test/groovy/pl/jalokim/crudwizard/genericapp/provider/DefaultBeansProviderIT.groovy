package pl.jalokim.crudwizard.genericapp.provider

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage

class DefaultBeansProviderIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private DefaultBeansProvider defaultBeansProvider

    def "should find default data storage in memory type and data storage meta model"() {
        when:
        def dataStorage = defaultBeansProvider.getDefaultDataStorage()

        then:
        dataStorage.getName() == InMemoryDataStorage.DEFAULT_DS_NAME
        dataStorage.getClass() == InMemoryDataStorage
    }
}

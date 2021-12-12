package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector

import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto

class DataStorageConnectorMetaModelDtoSamples {

    static DataStorageConnectorMetaModelDto createSampleDataStorageConnectorDto(ClassMetaModelDto classMetaModelInDataStorage,
        String queryName = null) {
        DataStorageConnectorMetaModelDto.builder()
            .classMetaModelInDataStorage(classMetaModelInDataStorage)
            .dataStorageMetaModel(DataStorageMetaModelDto.builder()
                .name("first-db")
                .className(InMemoryDataStorage.canonicalName)
                .build())
            .nameOfQuery(queryName)
            .build()
    }
}

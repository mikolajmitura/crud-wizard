package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector

import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto

class DataStorageConnectorMetaModelDtoSamples {

    static DataStorageConnectorMetaModelDto createSampleDataStorageConnectorDto(ClassMetaModelDto classMetaModelInDataStorage,
        String queryName = null, String dataStorageName = "first-db") {
        DataStorageConnectorMetaModelDto.builder()
            .classMetaModelInDataStorage(classMetaModelInDataStorage)
            .dataStorageMetaModel(createDataStorageMetaModelDto(dataStorageName))
            .nameOfQuery(queryName)
            .build()
    }

    static DataStorageConnectorMetaModelDto createSampleDataStorageConnectorDto(ClassMetaModelDto classMetaModelInDataStorage,
        DataStorageMetaModelDto dataStorageMetaModelDto,
        MapperMetaModelDto mapperMetaModelForReturn = null,
        MapperMetaModelDto mapperMetaModelForQuery = null,
        QueryProviderDto queryProviderDto = null,
        String queryName = null) {

        DataStorageConnectorMetaModelDto.builder()
            .classMetaModelInDataStorage(classMetaModelInDataStorage)
            .dataStorageMetaModel(dataStorageMetaModelDto)
            .mapperMetaModelForReturn(mapperMetaModelForReturn)
            .mapperMetaModelForQuery(mapperMetaModelForQuery)
            .queryProvider(queryProviderDto)
            .nameOfQuery(queryName)
            .build()
    }
}

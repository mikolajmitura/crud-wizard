package pl.jalokim.crudwizard.genericapp.metamodel.datastorage

import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage

class DataStorageMetaModelDtoSamples {

    static DataStorageMetaModelDto createDataStorageMetaModelDtoWithId(Long id) {
        DataStorageMetaModelDto.builder()
            .id(id)
            .build()
    }

    static DataStorageMetaModelDto createDataStorageMetaModelDto(String dataStorageName,
        String className = InMemoryDataStorage.canonicalName) {

        DataStorageMetaModelDto.builder()
            .name(dataStorageName)
            .className(className)
            .build()
    }

}

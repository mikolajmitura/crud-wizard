package pl.jalokim.crudwizard.genericapp.provider;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.RawAdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DefaultDataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;

@MetamodelService
@RequiredArgsConstructor
public class DefaultBeansConfigService {

    private static final String DEFAULT_DATA_STORAGE_ID = "default-data-storage-id";
    private static final String DEFAULT_GENERIC_MAPPER_ID = "default-generic-mapper-id";
    private static final String DEFAULT_GENERIC_SERVICE_ID = "default-data-service-id";
    private static final String DEFAULT_QUERY_PROVIDER_CLASS_NAME = "default-query-provider-class-name";
    private static final String DEFAULT_DATA_STORAGE_CONNECTORS_ID = "default-data-storage-connectors-id";

    private final List<DataStorage> dataStorages;
    private final DataStorageMetaModelService dataStorageMetaModelService;
    private final DataStorageMetaModelRepository dataStorageMetaModelRepository;
    private final RawAdditionalPropertyMapper rawAdditionalPropertyMapper;
    private final AdditionalPropertyRepository additionalPropertyRepository;
    private final MapperMetaModelService mapperMetaModelService;
    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final DataStorageConnectorMetaModelService dataStorageConnectorMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final GenericBeansProvider genericBeanProvider;

    // TODO test another
    //  default generic service with @primary as default when db config is null
    //  default data storage with @primary as default when db config is null
    //  default generic service with @primary as default when db config is null
    //  default generic service from db config
    //  default data storage from db config
    //  default generic service from db config
    //  default data connectors from db config

    public void saveAllDefaultMetaModels() {
        saveDefaultsDataStorage();
        saveDefaultsGenericMapper();
        saveDefaultQueryProvider();
        saveDefaultDataStorageConnectors();
        saveDefaultGenericService();
    }

    private void saveDefaultsDataStorage() {
        for (DataStorage dataStorage : dataStorages) {
            DataStorageMetaModelDto dataStorageDtoFromDataStorage = DataStorageMetaModelDto.builder()
                .name(dataStorage.getName())
                .className(dataStorage.getClassName())
                .build();
            if (!dataStorageMetaModelService.exists(dataStorageDtoFromDataStorage)) {
                Long dataStorageMetaModelId = dataStorageMetaModelService.createNewAndGetId(dataStorageDtoFromDataStorage);
                var defaultDataStorageMetaModelId = getDefaultDataStorageId();
                if (defaultDataStorageMetaModelId == null) {
                    saveNewConfiguration(DEFAULT_DATA_STORAGE_ID, dataStorageMetaModelId);
                }
            }
        }
    }

    private void saveDefaultsGenericMapper() {
        genericBeanProvider.getAllGenericMapperBeans().forEach(genericMapperBean ->
            genericMapperBean.getGenericMethodMetaModels().forEach(
                methodMetaModel -> {

                    MapperMetaModelDto mapperMetaModelDto = MapperMetaModelDto.builder()
                        .beanName(genericMapperBean.getBeanName())
                        .className(genericMapperBean.getClassName())
                        .methodName(methodMetaModel.getName())
                        .build();

                    if (!mapperMetaModelService.exists(mapperMetaModelDto)) {
                        Long genericMapperId = mapperMetaModelService.createNewAndGetId(mapperMetaModelDto);
                        var defaultGenericMapperId = getDefaultGenericMapperId();
                        if (defaultGenericMapperId == null) {
                            saveNewConfiguration(DEFAULT_GENERIC_MAPPER_ID, genericMapperId);
                        }
                    }
                }));
    }

    private void saveDefaultDataStorageConnectors() {
        var defaultDataStorageConnectorsId = getDefaultDataStorageConnectorsId();
        if (isEmpty(defaultDataStorageConnectorsId)) {
            Long[] newDefaultDataStorageConnectorsId = {
                dataStorageConnectorMetaModelService.saveNewDataStorageConnector(DataStorageConnectorMetaModelEntity.builder()
                    .dataStorageMetaModel(dataStorageMetaModelRepository.getOne(getDefaultDataStorageId()))
                    .mapperMetaModelForQuery(mapperMetaModelEntityRepository.getOne(getDefaultGenericMapperId()))
                    .mapperMetaModelForReturn(mapperMetaModelEntityRepository.getOne(getDefaultGenericMapperId()))
                    .queryProvider(QueryProviderEntity.builder()
                        .className(getDefaultQueryProviderClassName())
                        .build())
                    .build())
                    .getId()
            };
            saveNewConfiguration(DEFAULT_DATA_STORAGE_CONNECTORS_ID, newDefaultDataStorageConnectorsId, Long[].class);
        }
    }

    private void saveDefaultGenericService() {
        genericBeanProvider.getAllGenericServiceBeans().forEach(genericMapperBean ->
            genericMapperBean.getGenericMethodMetaModels().forEach(
                methodMetaModel -> {
                    ServiceMetaModelDto serviceMetaModelDto = ServiceMetaModelDto.builder()
                        .beanName(genericMapperBean.getBeanName())
                        .className(genericMapperBean.getClassName())
                        .methodName(methodMetaModel.getName())
                        .build();

                    if (!serviceMetaModelService.exists(serviceMetaModelDto)) {
                        Long genericServiceId = serviceMetaModelService.createNewAndGetId(serviceMetaModelDto);
                        var defaultGenericServiceId = getDefaultGenericServiceId();
                        if (defaultGenericServiceId == null) {
                            saveNewConfiguration(DEFAULT_GENERIC_SERVICE_ID, genericServiceId);
                        }
                    }
                }
            )
        );
    }

    private void saveDefaultQueryProvider() {
        String defaultQueryProviderClassName = getDefaultQueryProviderClassName();
        if (defaultQueryProviderClassName == null) {
            saveNewConfiguration(DEFAULT_QUERY_PROVIDER_CLASS_NAME, DefaultDataStorageQueryProvider.class.getCanonicalName(), String.class);
        }
    }

    private void saveNewConfiguration(String propertyName, Object configValue) {
        saveNewConfiguration(propertyName, configValue, Long.class);
    }

    private void saveNewConfiguration(String propertyName, Object configValue, Class<?> valueRealClass) {
        additionalPropertyRepository.save(
            rawAdditionalPropertyMapper.additionalPropertyToEntity(
                AdditionalPropertyDto.builder()
                    .name(propertyName)
                    .valueRealClassName(valueRealClass.getCanonicalName())
                    .valueAsObject(configValue)
                    .build()
            )
        );
    }

    public Long getDefaultDataStorageId() {
        return getConfigForDefault(DEFAULT_DATA_STORAGE_ID, Long.class);
    }

    public Long getDefaultGenericMapperId() {
        return getConfigForDefault(DEFAULT_GENERIC_MAPPER_ID, Long.class);
    }

    public List<Long> getDefaultDataStorageConnectorsId() {
        return Optional.ofNullable(getConfigForDefault(DEFAULT_DATA_STORAGE_CONNECTORS_ID, Long[].class))
            .map(arrayOfIds -> elements(arrayOfIds).asList())
            .orElse(null);
    }

    public Long getDefaultGenericServiceId() {
        return getConfigForDefault(DEFAULT_GENERIC_SERVICE_ID, Long.class);
    }

    public String getDefaultQueryProviderClassName() {
        return getConfigForDefault(DEFAULT_QUERY_PROVIDER_CLASS_NAME, String.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfigForDefault(String configKey, Class<T> propertyClass) {
        return additionalPropertyRepository.findByNameAndValueRealClassName(configKey, propertyClass.getCanonicalName())
            .map(rawAdditionalPropertyMapper::additionalPropertyToDto)
            .map(additionalPropertyToDto -> (T) additionalPropertyToDto.getRealValue())
            .orElse(null);
    }
}

package pl.jalokim.crudwizard.genericapp.provider;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.objectToRawJson;
import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.rawJsonToObject;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.crudwizard.genericapp.mapper.defaults.DefaultFinalGetIdAfterSaveMapper;
import pl.jalokim.crudwizard.genericapp.mapper.defaults.DefaultFinalJoinedRowOrDefaultMapper;
import pl.jalokim.crudwizard.genericapp.mapper.defaults.DefaultGenericMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.configuration.DefaultConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.configuration.DefaultConfigurationRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DefaultDataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;

@MetamodelService
@RequiredArgsConstructor
@Slf4j
public class DefaultBeansConfigService {

    private static final String DEFAULT_DATA_STORAGE_ID = "default-data-storage-id";
    private static final String DEFAULT_PERSIST_MAPPER_ID = "default-persist-mapper-id";
    private static final String DEFAULT_QUERY_MAPPER_ID = "default-query-mapper-id";
    private static final String DEFAULT_FINAL_MAPPER_ID = "default-final-mapper-id";
    private static final String DEFAULT_FINAL_GET_ID_AFTER_SAVE_MAPPER_ID = "default-final-get-id-after-save-mapper-id";
    private static final String DEFAULT_GENERIC_SERVICE_ID = "default-data-service-id";
    private static final String DEFAULT_QUERY_PROVIDER_CLASS_NAME = "default-query-provider-class-name";
    private static final String DEFAULT_DATA_STORAGE_CONNECTORS_ID = "default-data-storage-connectors-id";

    private final List<DataStorage> dataStorages;
    private final DataStorageMetaModelService dataStorageMetaModelService;
    private final DataStorageMetaModelRepository dataStorageMetaModelRepository;
    private final DefaultConfigurationRepository defaultConfigurationRepository;
    private final MapperMetaModelService mapperMetaModelService;
    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final DataStorageConnectorMetaModelService dataStorageConnectorMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final GenericBeansProvider genericBeanProvider;

    private static final Map<String, Class<?>> DEFAULT_MAPPER_BY_CONFIG = Map.of(
        DEFAULT_PERSIST_MAPPER_ID, DefaultGenericMapper.class,
        DEFAULT_QUERY_MAPPER_ID, DefaultGenericMapper.class,
        DEFAULT_FINAL_MAPPER_ID, DefaultFinalJoinedRowOrDefaultMapper.class,
        DEFAULT_FINAL_GET_ID_AFTER_SAVE_MAPPER_ID, DefaultFinalGetIdAfterSaveMapper.class
    );

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
        saveDefaultsGenericMappers();
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

    private void saveDefaultsGenericMappers() {
        DEFAULT_MAPPER_BY_CONFIG.forEach((mapperConfigName, mapperClass) -> {
            MapperMetaModelDto mapperMetaModelDto = MapperMetaModelDto.builder()
                .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                .mapperBeanAndMethod(BeanAndMethodDto.builder()
                    .beanName(UPPER_CAMEL.to(LOWER_CAMEL, mapperClass.getSimpleName()))
                    .className(mapperClass.getCanonicalName())
                    .methodName("mapToTarget")
                    .build())
                .build();

            Long idForMapperModel = mapperMetaModelService.findIdForMapperModel(mapperMetaModelDto);

            if (idForMapperModel == null) {
                idForMapperModel = mapperMetaModelService.createNewAndGetId(mapperMetaModelDto);
            }
            var defaultGenericMapperId = getDefaultMapperId(mapperConfigName);
            if (defaultGenericMapperId == null) {
                saveNewConfiguration(mapperConfigName, idForMapperModel);
            }
        });
    }

    private void saveDefaultDataStorageConnectors() {
        var defaultDataStorageConnectorsId = getDefaultDataStorageConnectorsId();
        if (isEmpty(defaultDataStorageConnectorsId)) {
            Long[] newDefaultDataStorageConnectorsId = {
                dataStorageConnectorMetaModelService.saveNewDataStorageConnector(DataStorageConnectorMetaModelEntity.builder()
                    .dataStorageMetaModel(dataStorageMetaModelRepository.getOne(getDefaultDataStorageId()))
                    .mapperMetaModelForQuery(mapperMetaModelEntityRepository.getOne(getDefaultMapperId(DEFAULT_QUERY_MAPPER_ID)))
                    .mapperMetaModelForPersist(mapperMetaModelEntityRepository.getOne(getDefaultMapperId(DEFAULT_PERSIST_MAPPER_ID)))
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
                        .serviceBeanAndMethod(BeanAndMethodDto.builder()
                            .beanName(genericMapperBean.getBeanName())
                            .className(genericMapperBean.getClassName())
                            .methodName(methodMetaModel.getMethodName())
                            .build())
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
        defaultConfigurationRepository.save(DefaultConfigurationEntity.builder()
            .propertyName(propertyName)
            .valueRealClassName(valueRealClass.getCanonicalName())
            .rawJson(objectToRawJson(configValue))
            .build());
    }

    public Long getDefaultDataStorageId() {
        return getConfigForDefault(DEFAULT_DATA_STORAGE_ID, Long.class);
    }

    public Long getDefaultMapperId(String configName) {
        return getConfigForDefault(configName, Long.class);
    }

    public Long getDefaultPersistMapperId() {
        return getDefaultMapperId(DEFAULT_PERSIST_MAPPER_ID);
    }

    public Long getDefaultQueryMapperId() {
        return getDefaultMapperId(DEFAULT_QUERY_MAPPER_ID);
    }

    public Long getDefaultFinalJoinedRowMapperId() {
        return getDefaultMapperId(DEFAULT_FINAL_MAPPER_ID);
    }

    public Long getDefaultFinalGetIdAfterSaveMapperId() {
        return getDefaultMapperId(DEFAULT_FINAL_GET_ID_AFTER_SAVE_MAPPER_ID);
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
        return defaultConfigurationRepository.findByPropertyNameAndValueRealClassName(configKey, propertyClass.getCanonicalName())
            .map(defaultConfigurationEntity -> (T) rawJsonToObject(
                defaultConfigurationEntity.getRawJson(),
                defaultConfigurationEntity.getValueRealClassName()))
            .orElse(null);
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeCreator.loadEndpointNodes;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getListFromContext;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.metamodels.ApiTagMetamodel;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;
import pl.jalokim.crudwizard.genericapp.provider.DefaultBeansConfigService;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@MetamodelService
@RequiredArgsConstructor
@Slf4j
public class MetaModelContextService implements ApplicationRunner {

    public AtomicReference<MetaModelContext> metaModelContextReference = new AtomicReference<>();

    private final DefaultBeansConfigService defaultBeansService;
    private final DataStorageMetaModelService dataStorageMetaModelService;
    private final ApiTagService apiTagService;
    private final ValidatorMetaModelService validatorMetaModelService;
    private final ClassMetaModelService classMetaModelService;
    private final MapperMetaModelService mapperMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final DataStorageConnectorMetaModelService dataStorageConnectorMetaModelService;
    private final EndpointMetaModelService endpointMetaModelService;
    private final InstanceLoader instanceLoader;

    @Override
    public void run(ApplicationArguments args) {
        reloadAll();
    }

    public synchronized void reloadAll() {
        defaultBeansService.saveAllDefaultMetaModels();
        MetaModelContext metaModelContext = new MetaModelContext();
        loadDataStorages(metaModelContext);
        loadApiTags(metaModelContext);
        loadValidatorMetaModelModels(metaModelContext);
        loadClassMetaModels(metaModelContext);
        loadMapperMetaModels(metaModelContext);
        loadServiceMetaModels(metaModelContext);
        loadDefaultQueryProvider(metaModelContext);
        loadDefaultDataStorageConnectorsMetaModel(metaModelContext);
        loadEndpointMetaModels(metaModelContext);
        loadEndpointNodes(metaModelContext);
        metaModelContextReference.set(metaModelContext);
        log.info("Reloaded meta model context");
    }

    private void loadDefaultQueryProvider(MetaModelContext metaModelContext) {
        String queryProviderClassName = defaultBeansService.getDefaultQueryProviderClassName();
        metaModelContext.setDefaultDataStorageQueryProvider(instanceLoader.createInstanceOrGetBean(queryProviderClassName));
    }

    private void loadDataStorages(MetaModelContext metaModelContext) {
        var dataStorages = new ModelsCache<DataStorageMetaModel>();
        var defaultDataStorageId = defaultBeansService.getDefaultDataStorageId();
        for (var dataStorageMetaModel : dataStorageMetaModelService.findAllMetaModels()) {
            dataStorages.put(dataStorageMetaModel.getId(), dataStorageMetaModel);
            if (defaultDataStorageId.equals(dataStorageMetaModel.getId())) {
                metaModelContext.setDefaultDataStorageMetaModel(dataStorageMetaModel);
            }
        }
        metaModelContext.setDataStorages(dataStorages);
    }

    private void loadApiTags(MetaModelContext metaModelContext) {
        var apiTags = new ModelsCache<ApiTagMetamodel>();
        for (var apiTag : apiTagService.findAll()) {
            apiTags.put(apiTag.getId(), apiTag);
        }
        metaModelContext.setApiTags(apiTags);
    }

    private void loadValidatorMetaModelModels(MetaModelContext metaModelContext) {
        var validatorMetaModels = new ModelsCache<ValidatorMetaModel>();
        for (var validatorMetaModel : validatorMetaModelService.findAllMetaModels()) {
            validatorMetaModels.put(validatorMetaModel.getId(), validatorMetaModel);
        }
        metaModelContext.setValidatorMetaModels(validatorMetaModels);
    }

    private void loadClassMetaModels(MetaModelContext metaModelContext) {
        var classMetaModels = new ModelsCache<ClassMetaModel>();
        for (var classMetaModel : classMetaModelService.findAllSwallowModels(metaModelContext)) {
            classMetaModels.put(classMetaModel.getId(), classMetaModel);
        }
        metaModelContext.setClassMetaModels(classMetaModels);

        classMetaModels.getObjectsById()
            .values()
            .forEach(classMetaModel -> {

                classMetaModel.setGenericTypes(
                    getListFromContext(classMetaModel.getGenericTypes(),
                        metaModelContext::getClassMetaModels,
                        ClassMetaModel::getId)
                );

                classMetaModel.getFields().forEach(
                    fieldMetaModel -> fieldMetaModel.setFieldType(
                        getFromContext(metaModelContext::getClassMetaModels, () -> fieldMetaModel.getFieldType().getId())
                    )
                );

                classMetaModel.setExtendsFromModels(
                    getListFromContext(classMetaModel.getExtendsFromModels(),
                        metaModelContext::getClassMetaModels,
                        ClassMetaModel::getId
                    )
                );
            });
    }

    private void loadMapperMetaModels(MetaModelContext metaModelContext) {
        var mapperMetaModels = new ModelsCache<MapperMetaModel>();
        var defaultGenericMapperId = defaultBeansService.getDefaultGenericMapperId();
        for (var mapperMetaModel : mapperMetaModelService.findAllMetaModels()) {
            mapperMetaModels.put(mapperMetaModel.getId(), mapperMetaModel);
            if (mapperMetaModel.getId().equals(defaultGenericMapperId)) {
                metaModelContext.setDefaultMapperMetaModel(mapperMetaModel);
            }
        }
        metaModelContext.setMapperMetaModels(mapperMetaModels);
    }

    private void loadServiceMetaModels(MetaModelContext metaModelContext) {
        var serviceMetaModels = new ModelsCache<ServiceMetaModel>();
        var defaultGenericServiceId = defaultBeansService.getDefaultGenericServiceId();
        for (var serviceMetaModel : serviceMetaModelService.findAllMetaModels()) {
            serviceMetaModels.put(serviceMetaModel.getId(), serviceMetaModel);
            if (serviceMetaModel.getId().equals(defaultGenericServiceId)) {
                metaModelContext.setDefaultServiceMetaModel(serviceMetaModel);
            }
        }
        metaModelContext.setServiceMetaModels(serviceMetaModels);
    }

    private void loadDefaultDataStorageConnectorsMetaModel(MetaModelContext metaModelContext) {
        metaModelContext.setDefaultDataStorageConnectorMetaModels(dataStorageConnectorMetaModelService.getAllMetaModels(
            metaModelContext, defaultBeansService.getDefaultDataStorageConnectorsId())
        );
    }

    private void loadEndpointMetaModels(MetaModelContext metaModelContext) {
        var endpointMetaModels = new ModelsCache<EndpointMetaModel>();
        for (var endpointMetaModel : endpointMetaModelService.findAllMetaModels(metaModelContext)) {
            endpointMetaModels.put(endpointMetaModel.getId(), endpointMetaModel);
        }
        metaModelContext.setEndpointMetaModels(endpointMetaModels);
    }

    public MetaModelContext getMetaModelContext() {
        return metaModelContextReference.get();
    }

    public DataStorage getDataStorageByName(String dataStorageName) {
        return getMetaModelContext().getDataStorages()
            .findOneBy(dataStorageMetaModel -> dataStorageMetaModel.getName().equals(dataStorageName))
            .getDataStorage();
    }

    public ClassMetaModel getClassMetaModelByName(String classMetaModelName) {
        return getMetaModelContext().getClassMetaModels()
            .findOneBy(classMetaModel -> classMetaModelName.equals(classMetaModel.getName()));
    }
}

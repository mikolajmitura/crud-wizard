package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getListFromContext;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;
import pl.jalokim.crudwizard.genericapp.provider.DataStorageMetaModelService;

@MetamodelService
@RequiredArgsConstructor
public class MetaModelContextService {

    public AtomicReference<MetaModelContext> metaModelContextReference = new AtomicReference<>();

    private final DataStorageMetaModelService dataStorageMetaModelService;
    private final ApiTagService apiTagService;
    private final ValidatorMetaModelService validatorMetaModelService;
    private final ClassMetaModelService classMetaModelService;
    private final MapperMetaModelService mapperMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final EndpointMetaModelService endpointMetaModelService;

    public synchronized void reloadAll() {
        MetaModelContext metaModelContext = new MetaModelContext();
        loadDataStorages(metaModelContext);
        loadApiTags(metaModelContext);
        loadValidatorMetaModelModels(metaModelContext);
        loadClassMetaModels(metaModelContext);
        loadMapperMetaModels(metaModelContext);
        loadServiceMetaModels(metaModelContext);
        loadEndpointMetaModels(metaModelContext);

        metaModelContextReference.set(metaModelContext);
    }

    private void loadDataStorages(MetaModelContext metaModelContext) {
        var dataStorages = new ModelsCache<DataStorageMetaModel>();
        for (var dataStorageMetaModel : dataStorageMetaModelService.findAll()) {
            dataStorages.put(dataStorageMetaModel.getId(), dataStorageMetaModel);
        }
        metaModelContext.setDataStorages(dataStorages);
    }

    private void loadApiTags(MetaModelContext metaModelContext) {
        var apiTags = new ModelsCache<ApiTagDto>();
        for (var apiTag : apiTagService.findAll()) {
            apiTags.put(apiTag.getId(), apiTag);
        }
        metaModelContext.setApiTags(apiTags);
    }

    private void loadValidatorMetaModelModels(MetaModelContext metaModelContext) {
        var validatorMetaModels = new ModelsCache<ValidatorMetaModel>();
        for (var validatorMetaModel : validatorMetaModelService.findAll()) {
            validatorMetaModels.put(validatorMetaModel.getId(), validatorMetaModel);
        }
        metaModelContext.setValidatorMetaModels(validatorMetaModels);
    }

    private void loadClassMetaModels(MetaModelContext metaModelContext) {
        var classMetaModels = new ModelsCache<ClassMetaModel>();
        for (var classMetaModel : classMetaModelService.findAllSwallow(metaModelContext)) {
            classMetaModels.put(classMetaModel.getId(), classMetaModel);
        }
        metaModelContext.setClassMetaModels(classMetaModels);

        classMetaModels.getModelsById()
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
        for (var mapperMetaModel : mapperMetaModelService.findAll()) {
            mapperMetaModels.put(mapperMetaModel.getId(), mapperMetaModel);
        }
        metaModelContext.setMapperMetaModels(mapperMetaModels);
    }

    private void loadServiceMetaModels(MetaModelContext metaModelContext) {
        var serviceMetaModels = new ModelsCache<ServiceMetaModel>();
        for (var serviceMetaModel : serviceMetaModelService.findAll(metaModelContext)) {
            serviceMetaModels.put(serviceMetaModel.getId(), serviceMetaModel);
        }
        metaModelContext.setServiceMetaModels(serviceMetaModels);
    }

    private void loadEndpointMetaModels(MetaModelContext metaModelContext) {
        var endpointMetaModels = new ModelsCache<EndpointMetaModelDto>();
        for (var endpointMetaModel : endpointMetaModelService.findAll(metaModelContext)) {
            endpointMetaModels.put(endpointMetaModel.getId(), endpointMetaModel);
        }
        metaModelContext.setEndpointMetaModels(endpointMetaModels);
    }
}

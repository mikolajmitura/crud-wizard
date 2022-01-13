package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;
import pl.jalokim.crudwizard.core.datetime.TimeProvider;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.core.validation.javax.groups.BeforeValidationInvoke;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntitySaveContext;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEvent;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;

@RequiredArgsConstructor
@MetamodelService
public class EndpointMetaModelService {

    private final EndpointMetaModelRepository endpointMetaModelRepository;
    private final ApiTagService apiTagService;
    private final EndpointMetaModelMapper endpointMetaModelMapper;
    private final ClassMetaModelService classMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final EndpointResponseMetaModelRepository endpointResponseMetaModelRepository;
    private final DataStorageConnectorMetaModelService dataStorageConnectorMetaModelService;
    private final ValidatorMetaModelService validatorMetaModelService;
    private final MapperMetaModelService mapperMetaModelService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TimeProvider timeProvider;
    private final ClassMetaModelEntitySaveContext classMetaModelEntitySaveContext;

    public Long createNewEndpoint(@BeforeValidationInvoke(beanType = BeforeEndpointValidatorUpdater.class, methodName = "beforeValidation")
        @Validated EndpointMetaModelDto createEndpointMetaModelDto) {
        try {
            classMetaModelEntitySaveContext.setupContext();
            final var endpointMetaModelEntity = endpointMetaModelMapper.toEntity(createEndpointMetaModelDto);

            endpointMetaModelEntity.setApiTag(apiTagService.saveNewOrLoadById(endpointMetaModelEntity.getApiTag()));

            endpointMetaModelEntity.setPayloadMetamodel(classMetaModelService.saveNewOrLoadById(endpointMetaModelEntity.getPayloadMetamodel()));

            elements(endpointMetaModelEntity.getPayloadMetamodelAdditionalValidators())
                .forEach(additionalValidatorsEntity -> validatorMetaModelService.saveOrCreateNewValidators(additionalValidatorsEntity.getValidators()));

            endpointMetaModelEntity.setServiceMetaModel(serviceMetaModelService.saveNewOrLoadById(endpointMetaModelEntity.getServiceMetaModel()));

            var responseMetaModel = endpointMetaModelEntity.getResponseMetaModel();
            if (responseMetaModel != null) {
                responseMetaModel.setClassMetaModel(classMetaModelService.saveNewOrLoadById(responseMetaModel.getClassMetaModel()));
                responseMetaModel.setMapperMetaModel(mapperMetaModelService.saveNewOrLoadById(responseMetaModel.getMapperMetaModel()));

                endpointMetaModelEntity.setResponseMetaModel(endpointResponseMetaModelRepository.save(responseMetaModel));
            }

            elements(endpointMetaModelEntity.getDataStorageConnectors())
                .forEachWithIndexed(indexedValue ->
                    endpointMetaModelEntity.getDataStorageConnectors().set(
                        indexedValue.getIndex(),
                        dataStorageConnectorMetaModelService.saveNewDataStorageConnector(indexedValue.getValue()))
                );

            endpointMetaModelEntity.setQueryArguments(classMetaModelService.saveNewOrLoadById(endpointMetaModelEntity.getQueryArguments()));
            endpointMetaModelEntity.setPathParams(classMetaModelService.saveNewOrLoadById(endpointMetaModelEntity.getPathParams()));

            EndpointMetaModelEntity newEndpoint = endpointMetaModelRepository.save(endpointMetaModelEntity);
            var newEndpointId = newEndpoint.getId();

            applicationEventPublisher.publishEvent(new MetaModelContextRefreshEvent(createNewEndpointReason(newEndpointId),
                timeProvider.getCurrentOffsetDateTime()));
            return newEndpointId;
        } finally {
            classMetaModelEntitySaveContext.clearSaveContext();
        }
    }

    public List<EndpointMetaModel> findAllMetaModels(MetaModelContext metaModelContext) {
        return elements(endpointMetaModelRepository.findAll())
            .map(endpointMetaModelEntity -> endpointMetaModelMapper.toFullMetaModel(metaModelContext, endpointMetaModelEntity))
            .asList();
    }

    public static String createNewEndpointReason(Long newEndpointId) {
        return "createNewEndpoint with id: " + newEndpointId;
    }

}

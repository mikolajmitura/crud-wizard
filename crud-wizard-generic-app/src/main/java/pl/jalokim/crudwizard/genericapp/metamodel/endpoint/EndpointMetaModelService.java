package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Objects.isNull;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;
import pl.jalokim.crudwizard.core.datetime.TimeProvider;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEvent;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;

@RequiredArgsConstructor
@MetamodelService
public class EndpointMetaModelService {

    private final EndpointMetaModelRepository endpointMetaModelRepository;
    private final ApiTagRepository apiTagRepository;
    private final EndpointMetaModelMapper endpointMetaModelMapper;
    private final ClassMetaModelService classMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final EndpointResponseMetaModelRepository endpointResponseMetaModelRepository;
    private final DataStorageConnectorMetaModelService dataStorageConnectorMetaModelService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TimeProvider timeProvider;

    public Long createNewEndpoint(@Validated EndpointMetaModelDto createEndpointMetaModelDto) {
        final var endpointMetaModelEntity = endpointMetaModelMapper.toEntity(createEndpointMetaModelDto);

        if (isNull(endpointMetaModelEntity.getApiTag().getId())) {
            endpointMetaModelEntity.setApiTag(apiTagRepository.save(endpointMetaModelEntity.getApiTag()));
        }

        var payloadMetamodel = endpointMetaModelEntity.getPayloadMetamodel();
        if (payloadMetamodel != null && payloadMetamodel.getId() == null) {
            endpointMetaModelEntity.setPayloadMetamodel(classMetaModelService.saveClassModel(payloadMetamodel));
        }

        var serviceMetaModel = endpointMetaModelEntity.getServiceMetaModel();
        if (serviceMetaModel != null && serviceMetaModel.getId() == null) {
            endpointMetaModelEntity.setServiceMetaModel(serviceMetaModelService.saveServiceMetaModel(serviceMetaModel));
        }

        var responseMetaModel = endpointMetaModelEntity.getResponseMetaModel();
        if (responseMetaModel != null && responseMetaModel.getId() == null) {
            var responseClassMetaModel = responseMetaModel.getClassMetaModel();
            if (responseClassMetaModel != null && responseClassMetaModel.getId() == null) {
                responseMetaModel.setClassMetaModel(classMetaModelService.saveClassModel(responseClassMetaModel));
            }
            endpointMetaModelEntity.setResponseMetaModel(endpointResponseMetaModelRepository.persist(responseMetaModel));
        }

        if (endpointMetaModelEntity.getDataStorageConnectors() != null) {
            elements(endpointMetaModelEntity.getDataStorageConnectors())
                .forEachWithIndexed(indexedValue ->
                    endpointMetaModelEntity.getDataStorageConnectors().set(
                        indexedValue.getIndex(),
                        dataStorageConnectorMetaModelService.saveNewDataStorageConnector(
                            indexedValue.getValue()))
                );
        }

        EndpointMetaModelEntity newEndpoint = endpointMetaModelRepository.persist(endpointMetaModelEntity);
        var newEndpointId = newEndpoint.getId();

        applicationEventPublisher.publishEvent(new MetaModelContextRefreshEvent(createNewEndpointReason(newEndpointId),
            timeProvider.getCurrentOffsetDateTime()));
        return newEndpointId;
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

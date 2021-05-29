package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Objects.isNull;

import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService;

@RequiredArgsConstructor
@MetamodelService
public class EndpointService {

    private final EndpointMetaModelRepository endpointMetaModelRepository;
    private final ApiTagRepository apiTagRepository;
    private final EndpointMapper endpointMapper;
    private final ClassMetaModelService classMetaModelService;
    private final ServiceMetaModelService serviceMetaModelService;
    private final EndpointResponseMetaModelRepository endpointResponseMetaModelRepository;

    public Long createNewEndpoint(CreateEndpointMetaModelDto createEndpointMetaModelDto) {
        var endpointMetaModelEntity = endpointMapper.toEntity(createEndpointMetaModelDto);

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
                responseClassMetaModel.setId(classMetaModelService.saveClassModel(responseClassMetaModel).getId());
            }
            endpointMetaModelEntity.setResponseMetaModel(endpointResponseMetaModelRepository.persist(responseMetaModel));
        }

        endpointMetaModelEntity = endpointMetaModelRepository.persist(endpointMetaModelEntity);

        return endpointMetaModelEntity.getId();
    }
}

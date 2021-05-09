package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.annotations.MaintenanceService;

@RequiredArgsConstructor
@MaintenanceService
public class EndpointService {

    private final EndpointMetaModelRepository endpointMetaModelRepository;
    private final EndpointMapper endpointMapper;

    public Long createNewEndpoint(CreateEndpointMetaModelDto createEndpointMetaModelDto) {
        EndpointMetaModelEntity endpointMetaModelEntity = endpointMapper.toEntity(createEndpointMetaModelDto);
        endpointMetaModelEntity = endpointMetaModelRepository.save(endpointMetaModelEntity);
        return endpointMetaModelEntity.getId();
    }
}

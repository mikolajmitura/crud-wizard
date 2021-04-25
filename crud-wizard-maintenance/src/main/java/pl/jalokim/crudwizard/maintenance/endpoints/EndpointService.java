package pl.jalokim.crudwizard.maintenance.endpoints;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class EndpointService {

    private final EndpointMetaModelRepository endpointMetaModelRepository;

    public Long createNewEndpoint(CreateEndpointMetaModelDto createEndpointMetaModelDto) {
        EndpointMetaModelEntity endpointMetaModelEntity = endpointMetaModelRepository.save(EndpointMetaModelEntity.builder()
            .baseUrl(createEndpointMetaModelDto.getBaseUrl())
            .build());
        return endpointMetaModelEntity.getId();
    }
}

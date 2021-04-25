package pl.jalokim.crudwizard.maintenance.endpoints;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EndpointRestController {

    private final EndpointService endpointService;

    @PostMapping
    public Long createNewEndpoint(@RequestBody @Valid CreateEndpointMetaModelDto createEndpointMetaModelDto) {
        return endpointService.createNewEndpoint(createEndpointMetaModelDto);
    }
}

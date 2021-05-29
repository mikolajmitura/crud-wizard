package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.CreateEndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointService;

@RestController
@RequestMapping("/maintenance/endpoints")
@RequiredArgsConstructor
public class EndpointRestController {

    private final EndpointService endpointService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createNewEndpoint(@RequestBody @Validated CreateEndpointMetaModelDto createEndpointMetaModelDto) {
        // TODO validation that there is not endpoint with that url and http method already
        // TODO should be validation that in normal spring mappings that endpoint (url and http method) haven't existed already
        return endpointService.createNewEndpoint(createEndpointMetaModelDto);
    }
}

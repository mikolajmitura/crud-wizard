package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService;

@RestController
@RequestMapping("/maintenance/endpoints")
@RequiredArgsConstructor
@Api(tags = "endpoint-management")
public class EndpointRestController {

    private final EndpointMetaModelService endpointService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creation of new endpoint with metamodels")
    public Long createNewEndpoint(@RequestBody EndpointMetaModelDto createEndpointMetaModelDto) {
        return endpointService.createNewEndpoint(createEndpointMetaModelDto);
    }
}

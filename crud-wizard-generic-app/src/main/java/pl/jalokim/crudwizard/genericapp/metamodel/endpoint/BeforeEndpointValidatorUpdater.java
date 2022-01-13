package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BeforeEndpointValidatorUpdater {

    public void beforeValidation(EndpointMetaModelDto createEndpointMetaModelDto) {
        Optional.ofNullable(createEndpointMetaModelDto.getQueryArguments())
            .ifPresent(queryArguments -> queryArguments.setName(createClassModelName(createEndpointMetaModelDto, "QueryArguments")));

        Optional.ofNullable(createEndpointMetaModelDto.getPathParams())
            .ifPresent(pathParams -> pathParams.setName(createClassModelName(createEndpointMetaModelDto, "PathParams")));
    }

    private static String createClassModelName(EndpointMetaModelDto createEndpointMetaModelDto, String typeName) {
        return elements(createEndpointMetaModelDto.getBaseUrl(), typeName, createEndpointMetaModelDto.getOperationName())
            .asConcatText("_");
    }
}

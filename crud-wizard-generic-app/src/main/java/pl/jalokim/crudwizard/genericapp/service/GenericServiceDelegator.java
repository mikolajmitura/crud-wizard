package pl.jalokim.crudwizard.genericapp.service;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils;
import pl.jalokim.crudwizard.genericapp.service.invoker.DelegatedServiceMethodInvoker;
import pl.jalokim.crudwizard.genericapp.service.translator.RawEntityObjectTranslator;
import pl.jalokim.crudwizard.genericapp.validation.generic.GenericValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenericServiceDelegator {

    private final DelegatedServiceMethodInvoker delegatedServiceMethodInvoker;
    private final EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils;
    private final RawEntityObjectTranslator rawEntityObjectTranslator;
    private final GenericValidator genericValidator;

    public ResponseEntity<Object> findAndInvokeHttpMethod(GenericServiceArgument genericServiceArgument) {
        var newGenericServiceArgument = searchForEndpointByRequest(genericServiceArgument);
        var foundEndpoint =  newGenericServiceArgument.getEndpointMetaModel();

        newGenericServiceArgument = newGenericServiceArgument.toBuilder()
            .httpQueryTranslated(rawEntityObjectTranslator.translateToRealObjects(
                genericServiceArgument.getHttpQueryParams(), foundEndpoint.getQueryArguments()))
            .requestBodyTranslated(rawEntityObjectTranslator.translateToRealObjects(
                genericServiceArgument.getRequestBody(), foundEndpoint.getPayloadMetamodel()))
            .build();

        genericValidator.validate(newGenericServiceArgument.getHttpQueryTranslated(), foundEndpoint.getQueryArguments());
        genericValidator.validate(newGenericServiceArgument.getRequestBodyTranslated(), foundEndpoint.getPayloadMetamodel());

        return delegatedServiceMethodInvoker.invokeMethod(newGenericServiceArgument);
    }

    private GenericServiceArgument searchForEndpointByRequest(GenericServiceArgument genericServiceArgument) {
        var httpMethod = HttpMethod.valueOf(genericServiceArgument.getRequest().getMethod());
        var requestedUrl = genericServiceArgument.getRequest().getRequestURI();
        var foundEndpointInfo = endpointMetaModelContextNodeUtils.findEndpointByUrl(requestedUrl, httpMethod);

        if (foundEndpointInfo.isNotFound()) {
            throw new EntityNotFoundException(createMessagePlaceholder("error.url.not.found", requestedUrl));
        }

        return genericServiceArgument.toBuilder()
            .endpointMetaModel(foundEndpointInfo.getEndpointMetaModel())
            .urlPathParams(foundEndpointInfo.getUrlPathParams())
            .build();
    }
}

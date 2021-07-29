package pl.jalokim.crudwizard.genericapp.service;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenericServiceDelegator {

    private final DelegatedServiceMethodInvoker delegatedServiceMethodInvoker;
    private final EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils;

    // TODO #03 test this whole method
    public ResponseEntity<Object> findAndInvokeHttpMethod(GenericServiceArgument genericServiceArgument) {
        var newGenericServiceArgument = searchForEndpointByRequest(genericServiceArgument);
        // TODO #01 translate fields, translate raw map<String, String> request to map with real classes.
        // TODO #02 invoke validation on fields and objects translated object
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

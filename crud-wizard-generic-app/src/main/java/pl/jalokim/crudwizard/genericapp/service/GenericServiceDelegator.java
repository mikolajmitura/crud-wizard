package pl.jalokim.crudwizard.genericapp.service;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject;
import pl.jalokim.crudwizard.core.exception.EntityNotFoundException;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNode;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.utils.collection.Elements;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenericServiceDelegator {

    private final MetaModelContextService metaModelContextService;
    private final DelegatedServiceMethodInvoker delegatedServiceMethodInvoker;
    private final ObjectMapper objectMapper;

    // TODO #05 test this whole method
    public ResponseEntity<Object> findAndInvokeHttpMethod(GenericServiceArgument genericServiceArgument) {
        var metaModelContext = metaModelContextService.getMetaModelContext();
        var newGenericServiceArgument = searchForEndpointByRequest(genericServiceArgument, metaModelContext);
        // TODO #03 translate fields, translate raw map<String, String> request to map with real classes.
        // TODO #04 invoke validation on fields and objects translated object
        return delegatedServiceMethodInvoker.invokeMethod(newGenericServiceArgument);
    }

    // TODO #01 test this method
    private GenericServiceArgument searchForEndpointByRequest(GenericServiceArgument genericServiceArgument, MetaModelContext metaModelContext) {
        var foundEndpointRef = new AtomicReference<EndpointMetaModel>();
        var httpMethod = HttpMethod.valueOf(genericServiceArgument.getRequest().getMethod());
        var requestedUrl = genericServiceArgument.getRequest().getRequestURI();
        var urlMetamodel = UrlModelResolver.resolveUrl(requestedUrl);
        var endpointNodeRef = new AtomicReference<>(metaModelContext.getEndpointMetaModelContextNode());
        var urlPathParamsValue = new ArrayList<String>();
        Elements.elements(urlMetamodel.getUrlParts())
            .forEachWithIndexed(urlPartIndexed -> {
                EndpointMetaModelContextNode nextEndpointNode = endpointNodeRef.get().getNodeByUrlPart(urlPartIndexed.getValue());
                if (nextEndpointNode == null) {
                    nextEndpointNode = endpointNodeRef.get().getVariableContextNode();
                    ofNullable(nextEndpointNode)
                        .ifPresent(notNullNextNode -> urlPathParamsValue.add(urlPartIndexed.getValue().getOriginalValue()));
                }
                verifyEndpointExistence(nextEndpointNode, requestedUrl);
                endpointNodeRef.set(nextEndpointNode);

                if (urlPartIndexed.isLast()) {
                    EndpointMetaModel foundEndpoint = endpointNodeRef.get().getEndpointByHttpMethod(httpMethod);
                    verifyEndpointExistence(foundEndpoint, requestedUrl);
                    foundEndpointRef.set(foundEndpoint);
                }
            });

        return getGenericServiceArgWithEndpointInfo(genericServiceArgument, foundEndpointRef, urlPathParamsValue);
    }

    private GenericServiceArgument getGenericServiceArgWithEndpointInfo(GenericServiceArgument genericServiceArgument,
        AtomicReference<EndpointMetaModel> foundEndpointRef, List<String> urlPathParamsValue) {
        var pathParamsMap = new HashMap<String, Object>();
        EndpointMetaModel endpointMetaModel = foundEndpointRef.get();
        List<FieldMetaModel> pathParamFields = endpointMetaModel.getPathParams().getFields();
        for (int pathParamIndex = 0; pathParamIndex < urlPathParamsValue.size(); pathParamIndex++) {
            String rawParamValue = urlPathParamsValue.get(pathParamIndex);
            FieldMetaModel fieldMetaModel = pathParamFields.get(pathParamIndex);
            Object convertedValue = objectMapper.convertValue(rawParamValue, fieldMetaModel.getFieldType().getRealClass());
            pathParamsMap.put(fieldMetaModel.getFieldName(), convertedValue);
        }

        return genericServiceArgument.toBuilder()
            .endpointMetaModel(foundEndpointRef.get())
            .urlPathParams(RawEntityObject.fromMap(pathParamsMap))
            .build();
    }

    private void verifyEndpointExistence(Object foundObject, String requestedUrl) {
        if (isNull(foundObject)) {
            throw new EntityNotFoundException(createMessagePlaceholder("error.url.not.found", requestedUrl));
        }
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPart;
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.utils.collection.CollectionUtils;

@Component
@RequiredArgsConstructor
public class EndpointMetaModelContextNodeUtils {

    private final JsonObjectMapper jsonObjectMapper;
    private final MetaModelContextService metaModelContextService;

    public EndpointMetaModel findEndpointMetaModelByUrlDuringCreate(String url, HttpMethod httpMethod) {
        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        var foundEndpointRef = new AtomicReference<EndpointMetaModel>();
        var urlMetamodel = UrlModelResolver.resolveUrl(url);
        var endpointNodeRef = new AtomicReference<>(metaModelContext.getEndpointMetaModelContextNode());
        List<UrlPart> urlParts = urlMetamodel.getUrlParts();

        for (int urlPartIndex = 0; urlPartIndex < urlParts.size(); urlPartIndex++) {
            UrlPart urlPart = urlParts.get(urlPartIndex);
            EndpointMetaModelContextNode nextEndpointNode = endpointNodeRef.get().getNodeByUrlPart(urlPart);
            if (nextEndpointNode == null && urlPart.isPathVariable()) {
                nextEndpointNode = endpointNodeRef.get().getVariableContextNode();
            }
            endpointNodeRef.set(nextEndpointNode);
            if (endpointNodeRef.get() == null) {
                break;
            }

            if (CollectionUtils.isLastIndex(urlParts, urlPartIndex)) {
                EndpointMetaModel foundEndpoint = endpointNodeRef.get().getEndpointByHttpMethod(httpMethod);
                foundEndpointRef.set(foundEndpoint);
            }
        }

        return foundEndpointRef.get();
    }

    public FoundEndpointMetaModel findEndpointForInvokeByUrl(String url, HttpMethod httpMethod) {
        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        var foundEndpointRef = new AtomicReference<EndpointMetaModel>();
        var urlMetamodel = UrlModelResolver.resolveUrl(url);
        var endpointNodeRef = new AtomicReference<>(metaModelContext.getEndpointMetaModelContextNode());
        var urlPathParamsValue = new ArrayList<String>();
        List<UrlPart> urlParts = urlMetamodel.getUrlParts();

        for (int urlPartIndex = 0; urlPartIndex < urlParts.size(); urlPartIndex++) {
            UrlPart urlPart = urlParts.get(urlPartIndex);
            EndpointMetaModelContextNode nextEndpointNode = endpointNodeRef.get().getNodeByUrlPart(urlPart);
            if (nextEndpointNode == null) {
                nextEndpointNode = endpointNodeRef.get().getVariableContextNode();
                ofNullable(nextEndpointNode)
                    .ifPresent(notNullNextNode -> urlPathParamsValue.add(urlPart.getOriginalValue()));
            }
            endpointNodeRef.set(nextEndpointNode);
            if (endpointNodeRef.get() == null) {
                break;
            }

            if (CollectionUtils.isLastIndex(urlParts, urlPartIndex)) {
                EndpointMetaModel foundEndpoint = endpointNodeRef.get().getEndpointByHttpMethod(httpMethod);
                foundEndpointRef.set(foundEndpoint);
            }
        }

        return createFoundEndpointMetaModel(foundEndpointRef.get(), urlPathParamsValue);
    }

    private FoundEndpointMetaModel createFoundEndpointMetaModel(EndpointMetaModel foundEndpoint, List<String> urlPathParamsValue) {
        var pathParamsMap = new HashMap<String, Object>();
        List<FieldMetaModel> pathParamFields = ofNullable(foundEndpoint)
            .map(endpointMeta -> ofNullable(endpointMeta.getPathParams())
                .map(ClassMetaModel::fetchAllFields)
                .orElse(List.of()))
            .orElse(List.of());

        if (CollectionUtils.isNotEmpty(pathParamFields)) {
            for (int pathParamIndex = 0; pathParamIndex < urlPathParamsValue.size(); pathParamIndex++) {
                String rawParamValue = urlPathParamsValue.get(pathParamIndex);
                FieldMetaModel fieldMetaModel = pathParamFields.get(pathParamIndex);
                String fieldName = fieldMetaModel.getFieldName();
                try {
                    Object convertedValue = jsonObjectMapper.convertToObject(ObjectNodePath.rootNode().nextNode(fieldName),
                        rawParamValue, fieldMetaModel.getFieldType().getRealClass());
                    pathParamsMap.put(fieldName, convertedValue);
                } catch (TechnicalException ex) {
                    throw new TechnicalException(
                        String.format("Problem with path variable name: %s in endpoint with url %s, method %s, invalid variable value: %s",
                            fieldName, foundEndpoint.getUrlMetamodel().getRawUrl(), foundEndpoint.getHttpMethod(), rawParamValue), ex);
                }
            }
        }

        return FoundEndpointMetaModel.builder()
            .endpointMetaModel(foundEndpoint)
            .urlPathParams(pathParamsMap)
            .build();
    }
}

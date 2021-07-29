package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.metamodels.url.UrlPart;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.utils.collection.CollectionUtils;

@Component
@RequiredArgsConstructor
public class EndpointMetaModelContextNodeUtils {

    private final ObjectMapper objectMapper;
    private final MetaModelContextService metaModelContextService;

    public FoundEndpointMetaModel findEndpointByUrl(String url, HttpMethod httpMethod) {
        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        var foundEndpointRef = new AtomicReference<EndpointMetaModel>();
        var urlMetamodel = UrlModelResolver.resolveUrl(url);
        var endpointNodeRef = new AtomicReference<>(metaModelContext.getEndpointMetaModelContextNode());
        var urlPathParamsValue = new ArrayList<String>();
        List<UrlPart> urlParts = urlMetamodel.getUrlParts();

        for (int urlPartIndex = 0; urlPartIndex < urlParts.size(); urlPartIndex++) {
            UrlPart urlPart = urlParts.get(urlPartIndex);
            EndpointMetaModelContextNode nextEndpointNode = endpointNodeRef.get().getNodeByUrlPart(urlPart);
            if (nextEndpointNode == null || urlPart.isPathVariable()) {
                nextEndpointNode = endpointNodeRef.get().getVariableContextNode();
                if (!urlPart.isPathVariable()) {
                    ofNullable(nextEndpointNode)
                        .ifPresent(notNullNextNode -> urlPathParamsValue.add(urlPart.getOriginalValue()));
                }
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
            .map(endpointMeta -> endpointMeta.getPathParams().getFields())
            .orElse(List.of());

        if (CollectionUtils.isNotEmpty(pathParamFields)) {
            for (int pathParamIndex = 0; pathParamIndex < urlPathParamsValue.size(); pathParamIndex++) {
                String rawParamValue = urlPathParamsValue.get(pathParamIndex);
                FieldMetaModel fieldMetaModel = pathParamFields.get(pathParamIndex);
                Object convertedValue = objectMapper.convertValue(rawParamValue, fieldMetaModel.getFieldType().getRealClass());
                pathParamsMap.put(fieldMetaModel.getFieldName(), convertedValue);
            }
        }

        return FoundEndpointMetaModel.builder()
            .endpointMetaModel(foundEndpoint)
            .urlPathParams(RawEntityObject.fromMap(pathParamsMap))
            .build();
    }
}

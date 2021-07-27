package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.metamodels.url.UrlPart.variableUrlPart;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.url.UrlPart;

@RequiredArgsConstructor
public class EndpointMetaModelContextNode {

    public static final String VARIABLE_URL_PART = "$_PATH_VARIABLE";

    private final Map<HttpMethod, EndpointMetaModel> endpointsByHttpMethod = new ConcurrentHashMap<>();
    private final Map<String, EndpointMetaModelContextNode> nextNodesByPath = new ConcurrentHashMap<>();
    private final UrlPart urlPart;

    public EndpointMetaModelContextNode putNextNodeOrGet(UrlPart urlPart) {
        EndpointMetaModelContextNode nodeToReturn;
        if (urlPart.isPathVariable()) {
            nodeToReturn = nextNodesByPath.get(VARIABLE_URL_PART);
            if (nodeToReturn == null) {
                nodeToReturn = new EndpointMetaModelContextNode(variableUrlPart(VARIABLE_URL_PART));
                nextNodesByPath.put(VARIABLE_URL_PART, nodeToReturn);
            }
        } else {
            nodeToReturn = nextNodesByPath.get(urlPart.getOriginalValue());
            if (nodeToReturn == null) {
                nodeToReturn = new EndpointMetaModelContextNode(urlPart);
                nextNodesByPath.put(urlPart.getOriginalValue(), nodeToReturn);
            }
        }
        return nodeToReturn;
    }

    public void putEndpointByMethod(EndpointMetaModel endpointMetaModel) {
        EndpointMetaModel currentEndpointMetaModel = endpointsByHttpMethod.get(endpointMetaModel.getHttpMethod());
        if (currentEndpointMetaModel == null) {
            endpointsByHttpMethod.put(endpointMetaModel.getHttpMethod(), endpointMetaModel);
        } else {
            // TODO #0 test this as well
            throw new IllegalStateException("Already exists endpoint with method: " + endpointMetaModel.getHttpMethod() + " and URL: "
                + endpointMetaModel.getUrlMetamodel().getRawUrl());
        }
    }
}

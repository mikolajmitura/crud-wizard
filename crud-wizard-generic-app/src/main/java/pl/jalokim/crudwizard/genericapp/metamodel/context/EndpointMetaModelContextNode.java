package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.core.metamodels.url.UrlPart.variableUrlPart;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.url.UrlPart;
import pl.jalokim.utils.collection.Elements;

@RequiredArgsConstructor
@Getter
public class EndpointMetaModelContextNode {

    public static final String VARIABLE_URL_PART = "$_PATH_VARIABLE";

    private final Map<HttpMethod, EndpointMetaModel> endpointsByHttpMethod = new ConcurrentHashMap<>();
    private final Map<String, EndpointMetaModelContextNode> nextNodesByPath = new ConcurrentHashMap<>();
    private final UrlPart urlPart;
    private final EndpointMetaModelContextNode parent;

    public EndpointMetaModelContextNode putNextNodeOrGet(UrlPart urlPart) {
        EndpointMetaModelContextNode nodeToReturn;
        if (urlPart.isPathVariable()) {
            nodeToReturn = nextNodesByPath.get(VARIABLE_URL_PART);
            if (nodeToReturn == null) {
                nodeToReturn = new EndpointMetaModelContextNode(variableUrlPart(VARIABLE_URL_PART), this);
                nextNodesByPath.put(VARIABLE_URL_PART, nodeToReturn);
            }
        } else {
            nodeToReturn = nextNodesByPath.get(urlPart.getOriginalValue());
            if (nodeToReturn == null) {
                nodeToReturn = new EndpointMetaModelContextNode(urlPart, this);
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
            throw new IllegalStateException("Already exists endpoint with method: " + endpointMetaModel.getHttpMethod()
                + " and URL: " + getRawUrl() + ", problematic URL: " + endpointMetaModel.getUrlMetamodel().getRawUrl()
            );
        }
    }

    public String getRawUrl() {
        var currentNode = this;
        var urlParts = new ArrayList<String>();
        while (currentNode != null) {
            urlParts.add(0, currentNode.getUrlPart().getOriginalValue());
            currentNode = currentNode.getParent();
        }
        return Elements.elements(urlParts)
            .asConcatText("/")
            .replace("//", "/");
    }
}

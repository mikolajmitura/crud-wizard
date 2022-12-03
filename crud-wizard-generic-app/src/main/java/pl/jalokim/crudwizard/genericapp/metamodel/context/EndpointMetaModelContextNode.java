package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.UrlPartMetaModel.createUrlPartMetamodel;
import static pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPart.normalUrlPart;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlPart;
import pl.jalokim.utils.collection.Elements;

@RequiredArgsConstructor
@Getter
public class EndpointMetaModelContextNode {

    public static final String VARIABLE_URL_PART = "$_PATH_VARIABLE";

    private final Map<HttpMethod, EndpointMetaModel> endpointsByHttpMethod = new ConcurrentHashMap<>();
    private final Map<String, EndpointMetaModelContextNode> nextNodesByPath = new ConcurrentHashMap<>();
    private final UrlPartMetaModel urlPartMetaModel;
    private final EndpointMetaModelContextNode parent;

    public static EndpointMetaModelContextNode createRootMetaModelNode() {
        return new EndpointMetaModelContextNode(createUrlPartMetamodel(normalUrlPart("/")), null);
    }

    public EndpointMetaModelContextNode putNextNodeOrGet(UrlPart urlPart) {
        UrlPartMetaModel urlPartMetamodel = createUrlPartMetamodel(urlPart);
        EndpointMetaModelContextNode nodeToReturn = getNodeByUrlPart(urlPart);
        if (nodeToReturn == null) {
            nodeToReturn = new EndpointMetaModelContextNode(createUrlPartMetamodel(urlPart), this);
            nextNodesByPath.put(urlPartMetamodel.getPathOrVariable(), nodeToReturn);
        }
        return nodeToReturn;
    }

    public EndpointMetaModelContextNode getNodeByUrlPart(UrlPart urlPart) {
        UrlPartMetaModel urlPartMetamodel = createUrlPartMetamodel(urlPart);
        return nextNodesByPath.get(urlPartMetamodel.getPathOrVariable());
    }

    public EndpointMetaModelContextNode getVariableContextNode() {
        return nextNodesByPath.get(VARIABLE_URL_PART);
    }

    public void putEndpointByMethod(EndpointMetaModel endpointMetaModel) {
        EndpointMetaModel currentEndpointMetaModel = getEndpointByHttpMethod(endpointMetaModel.getHttpMethod());
        if (currentEndpointMetaModel == null) {
            endpointsByHttpMethod.put(endpointMetaModel.getHttpMethod(), endpointMetaModel);
        } else {
            throw new IllegalStateException("Already exists endpoint with method: " + endpointMetaModel.getHttpMethod() +
                " and URL: " + getRawUrl() + ", problematic URL: " + endpointMetaModel.getUrlMetamodel().getRawUrl()
            );
        }
    }

    public EndpointMetaModel getEndpointByHttpMethod(HttpMethod httpMethod) {
        return endpointsByHttpMethod.get(httpMethod);
    }

    public String getRawUrl() {
        var currentNode = this;
        var urlParts = new ArrayList<String>();
        while (currentNode != null) {
            urlParts.add(0, currentNode.getUrlPartMetaModel().getUrlPart().getOriginalValue());
            currentNode = currentNode.getParent();
        }
        return Elements.elements(urlParts)
            .asConcatText("/")
            .replace("//", "/");
    }
}

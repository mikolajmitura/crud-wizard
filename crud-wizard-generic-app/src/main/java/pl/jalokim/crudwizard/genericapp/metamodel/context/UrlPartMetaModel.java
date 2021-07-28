package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNode.VARIABLE_URL_PART;

import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.url.UrlPart;

@Value
public class UrlPartMetaModel {

    UrlPart urlPart;
    String originalVariableName;

    public static UrlPartMetaModel createUrlPartMetamodel(UrlPart urlPart) {
        if (urlPart.isPathVariable()) {
            return new UrlPartMetaModel(UrlPart.variableUrlPart(VARIABLE_URL_PART), urlPart.getVariableName());
        }
        return new UrlPartMetaModel(urlPart, null);
    }

    public boolean isPathVariable() {
        return urlPart.isPathVariable();
    }

    public String getPathOrVariable() {
        if (isPathVariable()) {
            return urlPart.getVariableName();
        }
        return urlPart.getOriginalValue();
    }
}

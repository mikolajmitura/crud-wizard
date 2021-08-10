package pl.jalokim.crudwizard.core.metamodels.url;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Value;

@Value
public class UrlMetamodel {

    List<UrlPart> urlParts;
    String rawUrl;

    public List<String> getPathVariablesNames() {
        return elements(urlParts)
            .filter(UrlPart::isPathVariable)
            .map(UrlPart::getVariableName)
            .asList();
    }
}

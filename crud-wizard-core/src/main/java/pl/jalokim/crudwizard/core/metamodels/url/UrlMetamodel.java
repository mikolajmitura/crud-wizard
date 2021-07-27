package pl.jalokim.crudwizard.core.metamodels.url;

import static pl.jalokim.crudwizard.core.utils.ElementsUtils.nullableElements;

import java.util.List;
import lombok.Value;

@Value
public class UrlMetamodel {

    List<UrlPart> urlParts;
    String rawUrl;

    public List<String> getPathVariablesNames() {
        return nullableElements(urlParts)
            .filter(UrlPart::isPathVariable)
            .map(UrlPart::getVariableName)
            .asList();
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.url;

import static pl.jalokim.crudwizard.core.utils.StringHelper.replaceAllWithEmpty;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.metamodels.url.UrlMetamodel;
import pl.jalokim.crudwizard.core.metamodels.url.UrlPart;
import pl.jalokim.crudwizard.core.metamodels.url.UrlPart.UrlPartBuilder;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@UtilityClass
public class UrlModelResolver {

    private static final String PATH_VARIABLE_REGEX = "\\{([a-zA-Z0-9])+}";

    public static UrlMetamodel resolveUrl(String baseUrl) {
        List<UrlPart> urlParts = Elements.elements(baseUrl.split("/"))
            .filter(StringUtils::isNotBlank)
            .map(part -> newUrlPart(part, part.matches(PATH_VARIABLE_REGEX))).asList();
        return new UrlMetamodel(urlParts, baseUrl);
    }

    public static UrlPart newUrlPart(String originalUrl, boolean isPathVariable) {
        UrlPartBuilder urlPartBuilder = UrlPart.builder();
        if (isPathVariable) {
            urlPartBuilder.variableName(replaceAllWithEmpty(originalUrl, "{", "}"));
        }
        urlPartBuilder.originalValue(originalUrl);
        return urlPartBuilder.build();
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import static pl.jalokim.utils.collection.Elements.bySplitText;
import static pl.jalokim.utils.string.StringUtils.replaceAllWithEmpty;

import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.utils.constants.Constants;
import pl.jalokim.utils.string.StringUtils;

@UtilityClass
public class PropertyPathResolver {

    public static PropertyPath resolvePath(String pathToResolve) {
        return bySplitText(pathToResolve, "\\.")
            .flatMap(pathPart -> bySplitText(pathPart, "\\[")
                .mapWithIndexed(indexed -> {
                    if (!indexed.isFirst()) {
                        return Constants.LEFT_SQUARE_PARENTHESIS + indexed.getValue();
                    }
                    return indexed.getValue();
                }))
            .filter(StringUtils::isNotBlank)
            .reduce(PropertyPath.createRoot(),
                (propertyPath, pathPart) -> {
                    if (PropertyPath.isAllIndexes(pathPart)) {
                        return propertyPath.nextWithAllIndexes();
                    } else if (PropertyPath.isArrayElement(pathPart)) {
                        return propertyPath.nextWithIndex(Integer.parseInt(replaceAllWithEmpty(pathPart, "[", "]")));
                    }
                    return propertyPath.nextWithName(pathPart);
                },
                PropertyPath::concat);
    }
}

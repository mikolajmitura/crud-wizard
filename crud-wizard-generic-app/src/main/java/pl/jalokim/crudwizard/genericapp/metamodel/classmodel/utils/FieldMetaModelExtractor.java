package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.string.StringUtils.replaceAllWithEmpty;

import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath;
import pl.jalokim.utils.collection.Elements;

@UtilityClass
public class FieldMetaModelExtractor {

    public FieldMetaModel extractFieldMetaModel(ClassMetaModel classMetaModel, String path) {
        List<String> pathParts = Elements.bySplitText(replaceAllWithEmpty(path, " "), "\\.").asList();
        ClassMetaModel currentNode = classMetaModel;
        FieldMetaModel currentField = null;
        PropertyPath currentPath = PropertyPath.createRoot();

        for (String fieldName : pathParts) {
            currentField = currentNode.getFieldByName(fieldName);
            if (currentField == null) {
                throw new TechnicalException(createMessagePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    Map.of("currentPath", currentPath.buildFullPath(),
                        "fieldName", fieldName,
                        "currentNodeType", currentNode.getTypeDescription())));
            }
            currentPath = currentPath.nextWithName(fieldName);
            currentNode = currentField.getFieldType();
        }

        return currentField;
    }
}

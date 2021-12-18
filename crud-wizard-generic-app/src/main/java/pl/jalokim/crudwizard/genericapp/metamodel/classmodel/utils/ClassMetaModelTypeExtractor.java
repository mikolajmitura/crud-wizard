package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.TypeMetadata;
import pl.jalokim.utils.string.StringUtils;

@Component
@RequiredArgsConstructor
public class ClassMetaModelTypeExtractor {

    private final GenericModelTypeFactory genericModelTypeFactory;

    public Optional<TypeMetadata> getTypeByPath(ClassMetaModelDto classMetaModel, String path) {
        List<String> pathParts = Elements.bySplitText(path, "\\.").asList();
        GenericModelType currentNode = genericModelTypeFactory.fromDto(classMetaModel, null);
        String currentPath = "";
        for (String pathPart : pathParts) {
            TypeMetadata currentTypeMetadata = currentNode.extractTypeMetadata();
            if (currentTypeMetadata != null && isSimpleType(currentTypeMetadata.getRawType())) {
                throw new TechnicalException(createMessagePlaceholder("ClassMetaModelTypeExtractor.not.expected.any.field",
                    Map.of("currentPath", currentPath,
                        "currentNodeType", currentNode.getTypeName())));
            }

            var isSomeDynamicField = pathPart.startsWith("?");
            currentNode = getFieldType(currentPath, currentNode, pathPart, isSomeDynamicField);
            currentPath = concatPath(currentPath, pathPart);
            if (currentNode == null) {
                return Optional.empty();
            }
        }

        return Optional.of(convertFromWrapper(currentNode));
    }

    private GenericModelType getFieldType(String currentPath, GenericModelType currentNode, String pathPart, boolean suppressNotFound) {
        String fieldName = pathPart.replaceFirst("\\?", "");
        GenericModelType result = currentNode.getFieldTypeByName(fieldName, genericModelTypeFactory);

        if (result == null && !suppressNotFound) {
            throw new TechnicalException(createMessagePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                Map.of("currentPath", currentPath,
                    "fieldName", fieldName,
                    "currentNodeType", currentNode.getTypeName())));
        }
        return result;
    }

    private TypeMetadata convertFromWrapper(GenericModelType classTypeWrapper) {
        return Optional.ofNullable(classTypeWrapper.extractTypeMetadata())
            .orElseThrow(() -> new TechnicalException(wrapAsPlaceholder("ClassMetaModelTypeExtractor.not.as.raw.type")));
    }

    private String concatPath(String currentPath, String nextPathPart) {
        if (StringUtils.isBlank(currentPath)) {
            return nextPathPart;
        }
        return currentPath + "." + nextPathPart;
    }
}

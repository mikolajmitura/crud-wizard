package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.getFieldFromClassModel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByDeclaredFieldsResolver;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@Component
@RequiredArgsConstructor
public class ClassMetaModelTypeExtractor {

    private final ClassMetaModelMapper classMetaModelMapper;

    @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
    public Optional<ClassMetaModel> getTypeByPath(ClassMetaModelDto classMetaModelDto, String path) {

        ClassMetaModel currentClassMetadata = classMetaModelMapper.toModelFromDto(classMetaModelDto);
        String currentPath = "";
        List<String> pathParts = Elements.bySplitText(path, "\\.").asList();
        for (String pathPart : pathParts) {
            if (currentClassMetadata != null && currentClassMetadata.isSimpleType()) {
                throw new TechnicalException(createMessagePlaceholder("ClassMetaModelTypeExtractor.not.expected.any.field",
                    Map.of("currentPath", currentPath,
                        "currentNodeType", currentClassMetadata.getTypeDescription())));
            }

            var isSomeDynamicField = pathPart.startsWith("?");
            currentClassMetadata = getFieldType(currentPath, currentClassMetadata, pathPart, isSomeDynamicField);
            currentPath = concatPath(currentPath, pathPart);
            if (currentClassMetadata == null) {
                return Optional.empty();
            }
        }

        return Optional.of(currentClassMetadata);
    }

    private ClassMetaModel getFieldType(String currentPath, ClassMetaModel currentNode, String pathPart, boolean suppressNotFound) {
        String fieldName = pathPart.replaceFirst("\\?", "");
        FieldMetaModel result = getFieldFromClassModel(currentNode, fieldName, READ_FIELD_RESOLVER_CONFIG);

        if (result == null && !suppressNotFound) {

            if (currentNode.isOnlyRawClassModel()) {
                result = getFieldFromClassModel(currentNode, fieldName, FieldMetaResolverConfiguration.builder()
                    .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                    .fieldMetaResolverForClass(Map.of(currentNode.getRealClass(), ByDeclaredFieldsResolver.INSTANCE))
                    .build());
            }

            if (result == null) {
                throw new TechnicalException(createMessagePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    Map.of("currentPath", currentPath,
                        "fieldName", fieldName,
                        "currentNodeType", currentNode.getTypeDescription())));
            }

        }
        return Optional.ofNullable(result)
            .map(FieldMetaModel::getFieldType)
            .orElse(null);
    }

    private String concatPath(String currentPath, String nextPathPart) {
        if (StringUtils.isBlank(currentPath)) {
            return nextPathPart;
        }
        return currentPath + "." + nextPathPart;
    }
}

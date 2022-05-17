package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.crudwizard.core.config.jackson.StringBlankToNullModule.blankTextToNull;
import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.asLowerCamel;
import static pl.jalokim.crudwizard.genericapp.service.translator.JsonNodeUtils.getFieldsOfObjectNode;
import static pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath.rootNode;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isMapType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.EnumClassMetaModelValidator;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RawEntityObjectTranslator {

    /**
     * To add some custom deserializers and serializers inject ObjectMapper and add custom configuration to it.
     */
    private final JsonObjectMapper jsonObjectMapper;
    private final DefaultSubClassesForAbstractClassesConfig defaultClassesConfig;
    private final EnumClassMetaModelValidator enumClassMetaModelValidator;

    public <T> T translateToRealObjects(@Nullable JsonNode jsonNode, @Nullable ClassMetaModel nullableClassMetaModel) {
        if (jsonNode == null) {
            return null;
        }
        return (T) Optional.ofNullable(nullableClassMetaModel)
            .map(classMetaModel -> convertObject(rootNode(), jsonNode, classMetaModel))
            .orElse(null);
    }

    public <T> T translateToRealObjects(Map<String, Object> sourceMap, @Nullable ClassMetaModel nullableClassMetaModel) {
        return (T) Optional.ofNullable(nullableClassMetaModel)
            .map(classMetaModel -> translateToRealObjects(jsonObjectMapper.asJsonNode(rootNode(), sourceMap), classMetaModel))
            .orElse(null);
    }

    private Object convertObject(ObjectNodePath objectNodePath, JsonNode jsonNode, ClassMetaModel classMetaModel) {
        Object newObject = null;
        if (jsonNode != null) {
            if (classMetaModel.getRealClass() == null) {
                newObject = convertObjectBasedOnMetaData(objectNodePath, jsonNode, classMetaModel);
            } else {
                newObject = convertObjectBasedOnRealClass(objectNodePath, jsonNode, classMetaModel);
            }
        }
        return newObject;
    }

    private Object convertObjectBasedOnMetaData(ObjectNodePath objectNodePath, JsonNode jsonNode, ClassMetaModel classMetaModel) {
        if (classMetaModel.isGenericMetamodelEnum()) {
            TextNode textNode = jsonObjectMapper.castObjectTo(objectNodePath, jsonNode, TextNode.class);
            return enumClassMetaModelValidator.getEnumValueWhenIsValid(classMetaModel.getName(),
                textNode.textValue(),
                objectNodePath.getFullPath());
        } else {
            if (jsonNode instanceof NullNode) {
                return null;
            }
            ObjectNode mapObjectNode = jsonObjectMapper.castObjectTo(objectNodePath, jsonNode, ObjectNode.class);
            Map<Object, Object> targetMap = new LinkedHashMap<>();
            for (var jsonFieldEntry : getFieldsOfObjectNode(mapObjectNode)) {
                String fieldName = jsonFieldEntry.getName();
                JsonNode fieldValue = jsonFieldEntry.getJsonNode();
                FieldMetaModel fieldByName = getFieldByName(objectNodePath, classMetaModel, fieldName);
                ClassMetaModel fieldType = fieldByName.getFieldType();
                targetMap.put(fieldName, convertObject(objectNodePath.nextNode(fieldName), fieldValue, fieldType));
            }
            return targetMap;
        }
    }

    private FieldMetaModel getFieldByName(ObjectNodePath objectNodePath, ClassMetaModel classMetaModel, String fieldName) {
        return Optional.ofNullable(classMetaModel.getFieldByName(asLowerCamel(fieldName)))
            .orElseThrow(() -> new TechnicalException("Cannot find field with name: '" + fieldName + "' in path: '" + objectNodePath.getFullPath()
                + "' available fields: " + classMetaModel.getFieldNames() + " in named class meta model: '" + classMetaModel.getName() + "'"));
    }

    private Object convertObjectBasedOnRealClass(ObjectNodePath objectNodePath, JsonNode jsonNode, ClassMetaModel classMetaModel) {
        Class<?> realClass = classMetaModel.getRealClass();

        if (isMapType(realClass)) {
            ClassMetaModel typeOfMapKey = classMetaModel.getGenericTypes().get(0);
            ClassMetaModel typeOfMapValue = classMetaModel.getGenericTypes().get(1);
            Map<Object, Object> targetMap = (Map<Object, Object>) newInstance(realClass);
            ObjectNode mapObjectNode = jsonObjectMapper.castObjectTo(objectNodePath, jsonNode, ObjectNode.class);

            for (var jsonFieldEntry : getFieldsOfObjectNode(mapObjectNode)) {
                String fieldName = jsonFieldEntry.getName();
                JsonNode fieldValue = jsonFieldEntry.getJsonNode();
                var nextObjectNodePath = objectNodePath.nextNode(fieldName);
                targetMap.put(convertObject(nextObjectNodePath, new TextNode(fieldName), typeOfMapKey),
                    convertObject(nextObjectNodePath, fieldValue, typeOfMapValue));
            }

            return targetMap;
        } else if (isCollectionType(realClass)) {
            ClassMetaModel genericTypeOfCollection = classMetaModel.getGenericTypes().get(0);
            Collection<Object> targetCollection = (Collection<Object>) newInstance(realClass);

            if (jsonNode instanceof TextNode) {
                targetCollection.addAll(
                    Elements.bySplitText(jsonNode.textValue(), ",")
                        .map(String::trim)
                        .map(text -> convertObject(objectNodePath, TextNode.valueOf(text), genericTypeOfCollection))
                        .asList()
                );

            } else {
                ArrayNode arrayNode = jsonObjectMapper.castObjectTo(objectNodePath, jsonNode, ArrayNode.class);
                elements(arrayNode).forEachWithIndex((index, arrayNodeElement) ->
                    targetCollection.add(convertObject(objectNodePath.nextCollectionNode(index), arrayNodeElement, genericTypeOfCollection))
                );
            }

            return targetCollection;
        } else if (String.class.isAssignableFrom(realClass)) {
            return jsonNode instanceof ContainerNode ? jsonNode.toString() : blankTextToNull(jsonNode.textValue());
        }

        return jsonObjectMapper.convertToObject(objectNodePath, jsonNode, realClass);
    }

    private <T> T newInstance(Class<T> realClass) {
        Class<T> classToInit = (Class<T>) defaultClassesConfig.returnConfig().getOrDefault(realClass, realClass);
        return InvokableReflectionUtils.newInstance(classToInit);
    }
}

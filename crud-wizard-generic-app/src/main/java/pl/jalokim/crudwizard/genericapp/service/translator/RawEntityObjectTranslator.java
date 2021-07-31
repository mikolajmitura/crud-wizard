package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.crudwizard.core.config.jackson.StringBlankToNullModule.blankTextToNull;
import static pl.jalokim.crudwizard.genericapp.service.translator.JsonNodeUtils.getFieldsOfObjectNode;
import static pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath.rootNode;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.newInstance;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isMapType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;

@Component
@RequiredArgsConstructor
public class RawEntityObjectTranslator {

    /**
     * To add some custom deserializers and serializers inject ObjectMapper and add custom configuration to it.
     */
    private final JsonObjectMapper jsonObjectMapper;

    @SuppressWarnings("unchecked")
    public <T> T translateToRealObjects(JsonNode jsonNode, ClassMetaModel classMetaModel) {
        return (T) convertObject(rootNode(), jsonNode, classMetaModel);
    }

    @SuppressWarnings("unchecked")
    public <T> T translateToRealObjects(Map<String, Object> sourceMap, ClassMetaModel classMetaModel) {
        return (T) translateToRealObjects(jsonObjectMapper.asJsonNode(rootNode(), sourceMap), classMetaModel);
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

    private FieldMetaModel getFieldByName(ObjectNodePath objectNodePath, ClassMetaModel classMetaModel, String fieldName) {
        try {
            return classMetaModel.getFieldByName(fieldName);
        } catch (NoSuchElementException ex) {
            throw new TechnicalException("Cannot find field with name: '" + fieldName + "' in path: '" + objectNodePath.getFullPath()
                + "' available fields: " + classMetaModel.getFieldNames() + " in named class meta model: '" + classMetaModel.getName() + "'", ex);
        }
    }

    @SuppressWarnings("unchecked")
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
            ArrayNode arrayNode = jsonObjectMapper.castObjectTo(objectNodePath, jsonNode, ArrayNode.class);

            elements(arrayNode).forEach((index, arrayNodeElement) ->
                targetCollection.add(convertObject(objectNodePath.nextCollectionNode(index), arrayNodeElement, genericTypeOfCollection))
            );
            return targetCollection;
        } else if (String.class.isAssignableFrom(realClass)) {
            return jsonNode instanceof ContainerNode ? jsonNode.toString() : blankTextToNull(jsonNode.textValue());
        }

        return jsonObjectMapper.convertToObject(objectNodePath, jsonNode, realClass);
    }
}
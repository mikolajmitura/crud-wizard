package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath.rootNode;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.newInstance;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isMapType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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

    public Map<String, Object> translateToRealObjects(Map<String, Object> sourceMap, ClassMetaModel classMetaModel) {
        return convertObject(rootNode(), sourceMap, classMetaModel);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertObject(ObjectNodePath objectNodePath, Object sourceObject, ClassMetaModel classMetaModel) {
        Object newObject = null;
        if (sourceObject != null) {
            if (classMetaModel.getRealClass() == null) {
                newObject = convertObjectBasedOnMetaData(objectNodePath, sourceObject, classMetaModel);
            } else {
                newObject = convertObjectBasedOnRealClass(objectNodePath, sourceObject, classMetaModel);
            }
        }
        return (T) newObject;
    }

    private Object convertObjectBasedOnMetaData(ObjectNodePath objectNodePath, Object sourceObject, ClassMetaModel classMetaModel) {
        Map<String, Object> sourceMap = jsonObjectMapper.castObjectTo(objectNodePath, sourceObject, Map.class);
        Map<Object, Object> targetMap = new LinkedHashMap<>();
        for (var sourceMapEntry : sourceMap.entrySet()) {
            String fieldName = sourceMapEntry.getKey();
            Object fieldValue = sourceMapEntry.getValue();
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
    private Object convertObjectBasedOnRealClass(ObjectNodePath objectNodePath, Object sourceObject, ClassMetaModel classMetaModel) {
        Class<?> realClass = classMetaModel.getRealClass();
        if (isMapType(realClass)) {
            ClassMetaModel typeOfMapKey = classMetaModel.getGenericTypes().get(0);
            ClassMetaModel typeOfMapValue = classMetaModel.getGenericTypes().get(1);
            Map<Object, Object> targetMap = (Map<Object, Object>) newInstance(realClass);
            Map<Object, Object> sourceMap = jsonObjectMapper.castObjectTo(objectNodePath, sourceObject, Map.class);
            for (Entry<Object, Object> mapEntry : sourceMap.entrySet()) {
                var nextObjectNodePath = objectNodePath.nextNode(mapEntry.getKey().toString());
                targetMap.put(convertObject(nextObjectNodePath, mapEntry.getKey(), typeOfMapKey),
                    convertObject(nextObjectNodePath, mapEntry.getValue(), typeOfMapValue));
            }
            return targetMap;
        } else if (isCollectionType(realClass)) {
            ClassMetaModel genericTypeOfCollection = classMetaModel.getGenericTypes().get(0);
            Collection<Object> targetCollection = (Collection<Object>) newInstance(realClass);
            Collection<Object> sourceCollection = jsonObjectMapper.castObjectTo(objectNodePath, sourceObject, Collection.class);
            elements(sourceCollection).forEach((index, sourceElement) ->
                targetCollection.add(convertObject(objectNodePath.nextCollectionNode(index), sourceElement, genericTypeOfCollection))
            );
            return targetCollection;
        }
        return jsonObjectMapper.convertToObject(objectNodePath, sourceObject, realClass);
    }
}

package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.utils.reflection.InvokableReflectionUtils.newInstance;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isMapType;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;

@Component
@RequiredArgsConstructor
public class RawEntityObjectTranslator {

    // TODO #01 translate fields, translate raw map<String, String> request to map with real classes. And Test it
    // TODO #01_1 translation failed (cannot find field name in class metamodel)
    // TODO #01_2 invalid field conversion
    // TODO #01_3 usage of other field converters translate some map node to real object bean for example to AdressDto

    private final ObjectMapper objectMapper;

    public Map<String, Object> translateToRealObjects(Map<String, Object> sourceMap, ClassMetaModel classMetaModel) {
        return convertObject(sourceMap, classMetaModel);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertObject(Object sourceObject, ClassMetaModel classMetaModel) {
        Object newObject = null;
        if (sourceObject != null) {
            if (classMetaModel.getRealClass() == null) {
                newObject = convertObjectBasedOnMetaData(sourceObject, classMetaModel);
            } else {
                newObject = convertObjectBasedOnRealClass(sourceObject, classMetaModel);
            }
        }
        return (T) newObject;
    }

    @SuppressWarnings("unchecked")
    private Object convertObjectBasedOnMetaData(Object sourceObject, ClassMetaModel classMetaModel) {
        Map<String, Object> sourceMap = (Map<String, Object>) sourceObject;
        Map<Object, Object> targetMap = new LinkedHashMap<>();
        for (var sourceMapEntry : sourceMap.entrySet()) {
            String fieldName = sourceMapEntry.getKey();
            Object fieldValue = sourceMapEntry.getValue();
            FieldMetaModel fieldByName = classMetaModel.getFieldByName(fieldName);
            ClassMetaModel fieldType = fieldByName.getFieldType();
            targetMap.put(fieldName, convertObject(fieldValue, fieldType));
        }
        return targetMap;
    }

    @SuppressWarnings("unchecked")
    private Object convertObjectBasedOnRealClass(Object sourceObject, ClassMetaModel classMetaModel) {
        Class<?> realClass = classMetaModel.getRealClass();
        if (isMapType(realClass)) {
            ClassMetaModel typeOfMapKey = classMetaModel.getGenericTypes().get(0);
            ClassMetaModel typeOfMapValue = classMetaModel.getGenericTypes().get(1);
            Map<Object, Object> targetMap = (Map<Object, Object>) newInstance(realClass);
            Map<Object, Object> sourceMap = (Map<Object, Object>) sourceObject;
            for (Entry<Object, Object> mapEntry : sourceMap.entrySet()) {
                targetMap.put(convertObject(mapEntry.getKey(), typeOfMapKey),
                    convertObject(mapEntry.getValue(), typeOfMapValue));
            }
            return targetMap;
        } else if (isCollectionType(realClass)) {
            ClassMetaModel genericTypeOfCollection = classMetaModel.getGenericTypes().get(0);
            Collection<Object> targetCollection = (Collection<Object>) newInstance(realClass);
            Collection<Object> sourceCollection = (Collection<Object>) sourceObject;
            sourceCollection.forEach(sourceElement ->
                targetCollection.add(convertObject(sourceElement, genericTypeOfCollection))
            );
            return targetCollection;
        }

        return objectMapper.convertValue(sourceObject, realClass);
    }
}

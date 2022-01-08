package pl.jalokim.crudwizard.core.utils;

import java.util.Map;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;

@UtilityClass
public class DataFieldsHelper {

    @SuppressWarnings("unchecked")
    public static Object getFieldValue(Object targetObject, String fieldName) {
        if (targetObject instanceof Map) {
            var objectAsMap = (Map<String, Object>) targetObject;
            return objectAsMap.get(fieldName);
        }
        return InvokableReflectionUtils.getValueOfField(targetObject, fieldName);
    }

    @SuppressWarnings("unchecked")
    public static void setFieldValue(Object targetObject, String fieldName, Object valueOfField) {
        if (targetObject instanceof Map) {
            var objectAsMap = (Map<String, Object>) targetObject;
            objectAsMap.put(fieldName, valueOfField);
        } else {
            InvokableReflectionUtils.setValueForField(targetObject, fieldName, valueOfField);
        }
    }
}

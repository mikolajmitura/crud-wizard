package pl.jalokim.crudwizard.genericapp.service.translator;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;

@Component
@RequiredArgsConstructor
public class JsonObjectMapper {

    /**
     * To add some custom deserializers and serializers inject ObjectMapper and add custom configuration to it.
     */
    private final ObjectMapper objectMapper;

    public Object convertToObject(ObjectNodePath objectNodePath, Object sourceObject, Class<?> targetClass) {
        String jsonValue = asJsonValue(objectNodePath, sourceObject);
        if (String.class.isAssignableFrom(targetClass) && !isSimpleType(sourceObject.getClass())) {
            return jsonValue;
        }
        try {
            return objectMapper.readValue(jsonValue, targetClass);
        } catch (JsonProcessingException e) {
            throw cannotConvertException(objectNodePath, targetClass, jsonValue, e);
        }
    }

    // TODO #001 impl this method in future and return TechnicalException message
    public Object convertToObject(String jsonValue, JavaType javaType) {
        try {
            return objectMapper.readValue(jsonValue, javaType);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T castObjectTo(ObjectNodePath objectNodePath, Object sourceObject, Class<?> targetClass) {
        try {
            return (T) targetClass.cast(sourceObject);
        } catch (ClassCastException ex) {
            throw new TechnicalException("Cannot cast from: " + sourceObject.getClass().getCanonicalName()
                + " to " + targetClass.getCanonicalName() + " in path: " + objectNodePath.getFullPath()
                + " invalid json part: " + asJsonValue(objectNodePath, sourceObject), ex);
        }
    }

    public static TechnicalException cannotConvertException(ObjectNodePath objectNodePath, Class<?> targetClass, String jsonValue, JsonProcessingException e) {
        return new TechnicalException("Cannot convert from value: '" + jsonValue + "' to class " + targetClass.getCanonicalName()
            + " in path: " + objectNodePath.getFullPath(), e);
    }

    public String asJsonValue(ObjectNodePath objectNodePath, Object sourceObject) {
        try {
            return objectMapper.writeValueAsString(sourceObject);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("Cannot write object " + sourceObject + " as json value in path " + objectNodePath.getFullPath(), e);
        }
    }

    public JsonNode asJsonNode(ObjectNodePath objectNodePath, Object sourceObject) {
        return asJsonNode(objectNodePath, asJsonValue(objectNodePath, sourceObject));
    }

    public JsonNode asJsonNode(ObjectNodePath objectNodePath, String jsonValue) {
        try {
            return objectMapper.readTree(jsonValue);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("Cannot write object " + jsonValue + " as json value in path " + objectNodePath.getFullPath(), e);
        }
    }

    public JavaType createJavaType(Type type, Class<?> contextClass) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
    }
}

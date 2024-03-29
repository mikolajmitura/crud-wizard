package pl.jalokim.crudwizard.genericapp.service.translator;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;

@Component
@RequiredArgsConstructor
public class JsonObjectMapper {

    private static final AtomicReference<JsonObjectMapper> MAPPER_HOLDER = new AtomicReference<>();

    /**
     * To add some custom deserializers and serializers inject ObjectMapper and add custom configuration to it.
     */
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void postConstruct() {
        MAPPER_HOLDER.set(this);
    }

    public Object convertToObject(ObjectNodePath objectNodePath, Object sourceObject, Class<?> targetClass) {
        String jsonValue = asJsonValue(objectNodePath, sourceObject);
        if (String.class.isAssignableFrom(targetClass) && !isSimpleType(sourceObject.getClass())) {
            return jsonValue;
        }
        try {
            return objectMapper.readValue(jsonValue, targetClass);
        } catch (JsonProcessingException e) {
            throw cannotConvertException(objectNodePath, targetClass.getCanonicalName(), jsonValue, e);
        }
    }

    public Object convertToObject(ObjectNodePath objectNodePath, Object sourceObject, JavaType jacksonJavaType) {
        String jsonValue = asJsonValue(objectNodePath, sourceObject);
        try {
            return objectMapper.readValue(jsonValue, jacksonJavaType);
        } catch (JsonProcessingException e) {
            throw cannotConvertException(objectNodePath, jacksonJavaType.getGenericSignature(), jsonValue, e);
        }
    }

    public Object convertToObject(String jsonValue, JavaTypeMetaModel javaTypeMetaModel) {
        try {
            if (javaTypeMetaModel.isRawClass()) {
                if (String.class.isAssignableFrom(javaTypeMetaModel.getRawClass()) && !jsonValue.trim().matches("\"(.)+\"")) {
                    var wrappedJsonValue = String.format("\"%s\"", jsonValue);
                    return objectMapper.readValue(wrappedJsonValue, javaTypeMetaModel.getRawClass());
                }
                return objectMapper.readValue(jsonValue, javaTypeMetaModel.getRawClass());
            } else {
                return objectMapper.readValue(jsonValue, javaTypeMetaModel.getJacksonJavaType());
            }
        } catch (JsonProcessingException e) {
            throw new TechnicalException(String.format("Cannot convert from: %s to %s", jsonValue, javaTypeMetaModel), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T castObjectTo(ObjectNodePath objectNodePath, Object sourceObject, Class<?> targetClass) {
        try {
            return (T) targetClass.cast(sourceObject);
        } catch (ClassCastException ex) {
            throw new TechnicalException("Cannot cast from: " + sourceObject.getClass().getCanonicalName() +
                " to " + targetClass.getCanonicalName() + inJsonPath(objectNodePath) +
                " invalid json part: " + asJsonValue(objectNodePath, sourceObject), ex);
        }
    }

    public static TechnicalException cannotConvertException(ObjectNodePath objectNodePath, String typeDescription, String jsonValue,
        JsonProcessingException e) {
        return new TechnicalException("Cannot convert from value: '" + jsonValue +
            "' to type: " + typeDescription +
            inJsonPath(objectNodePath), e);
    }

    public String asJsonValue(ObjectNodePath objectNodePath, Object sourceObject) {
        try {
            return objectMapper.writeValueAsString(sourceObject);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("Cannot write object " + sourceObject + " as json value" + inJsonPath(objectNodePath), e);
        }
    }

    public JsonNode asJsonNode(String jsonValue) {
        return asJsonNode(ObjectNodePath.rootNode(), jsonValue);
    }

    public JsonNode asJsonNode(ObjectNodePath objectNodePath, Object sourceObject) {
        return asJsonNode(objectNodePath, asJsonValue(objectNodePath, sourceObject));
    }

    public JsonNode asJsonNode(ObjectNodePath objectNodePath, String jsonValue) {
        try {
            return objectMapper.readTree(jsonValue);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("Cannot write object " + jsonValue + " as json value" + inJsonPath(objectNodePath), e);
        }
    }

    public static String inJsonPath(ObjectNodePath objectNodePath) {
        return EMPTY.equals(objectNodePath.getFullPath()) ? EMPTY : " in path: " + objectNodePath.getFullPath();
    }

    public JavaType createJavaType(Type type, Class<?> contextClass) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
    }

    @SuppressWarnings("PMD.SingletonClassReturningNewInstance")
    public static JsonObjectMapper getInstance() {
        JsonObjectMapper jsonObjectMapper = MAPPER_HOLDER.get();
        if (jsonObjectMapper == null) {
            throw new IllegalStateException("MAPPER_HOLDER is have not initialized yet!");
        }
        return jsonObjectMapper;
    }
}

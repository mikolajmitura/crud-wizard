package pl.jalokim.crudwizard.genericapp.util;

import static org.apache.commons.lang3.tuple.ImmutablePair.of;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ReflectionOperationException;
import pl.jalokim.utils.reflection.TypeMetadata;
import pl.jalokim.utils.string.StringUtils;

public class CustomPropertiesResolver {

    private static final Map<Class<?>, Function<String, Object>> OBJECT_FACTORY_BY_CLASS_TYPE = Map.ofEntries(
        of(String.class, rawValueAsText -> rawValueAsText),
        of(Long.class, Long::valueOf),
        of(long.class, Long::parseLong),
        of(Integer.class, Integer::valueOf),
        of(int.class, Integer::parseInt),
        of(Double.class, Double::valueOf),
        of(double.class, Double::parseDouble),
        of(Short.class, Short::valueOf),
        of(short.class, Short::parseShort),
        of(Boolean.class, Boolean::valueOf),
        of(boolean.class, Boolean::parseBoolean)
    );

    public static void bind(Object objectProperties, Environment environment) {
        Class<?> propertiesClass = objectProperties.getClass();
        ConfigurationProperties annotation = propertiesClass.getAnnotation(ConfigurationProperties.class);
        String propertyPrefix = annotation.prefix();
        if (StringUtils.isBlank(propertyPrefix)) {
            propertyPrefix = annotation.value();
        }

        List<MapPropertySource> propertySources = getMapPropertySources((AbstractEnvironment) environment);

        Map<String, PropertySourceOriginValue> properties = getValueOfProperties(propertyPrefix, propertySources);

        bindProperties(properties,
            ObjectWithMeta.builder()
                .realObject(objectProperties)
                .objectMetadata(MetadataReflectionUtils.getTypeMetadataFromClass(propertiesClass))
                .build());
    }

    private static Map<String, PropertySourceOriginValue> getValueOfProperties(String propertyPrefix, List<MapPropertySource> propertySources) {
        Map<String, PropertySourceOriginValue> properties = new HashMap<>();
        for (MapPropertySource propertySource : propertySources) {
            Map<String, Object> mapFromPropertySource = propertySource.getSource();
            for (String keyProperty : mapFromPropertySource.keySet()) {
                if (keyProperty.startsWith(propertyPrefix)) {
                    Elements<String> keyPropertyParts = Elements.elements(keyProperty.replace(propertyPrefix + ".", "")
                        .split("\\."));
                    AtomicReference<Map<String, PropertySourceOriginValue>> currentNode = new AtomicReference<>(properties);
                    keyPropertyParts.forEachWithIndexed(element -> {
                        String propertyPart = element.getValue();
                        PropertySourceOriginValue node = currentNode.get().get(propertyPart);
                        if (node == null) {
                            if (element.isLast()) {
                                currentNode.get().put(propertyPart, getOriginValue(propertySource, mapFromPropertySource, keyProperty));
                            } else {
                                HashMap<String, PropertySourceOriginValue> newNode = new HashMap<>();
                                currentNode.get().put(propertyPart, new PropertySourceOriginValue(propertySource.getName(), keyProperty, newNode));
                                currentNode.set(newNode);
                            }
                        } else {
                            if (node.getPropertyValue() instanceof Map) {
                                currentNode.set(getAsMap(node.getPropertyValue()));
                            } else {
                                currentNode.get().put(propertyPart, getOriginValue(propertySource, mapFromPropertySource, keyProperty));
                            }
                        }
                    });
                }
            }
        }
        return properties;
    }

    private static PropertySourceOriginValue getOriginValue(MapPropertySource propertySource, Map<String,
        Object> mapFromPropertySource, String keyProperty) {
        return new PropertySourceOriginValue(propertySource.getName(),
            keyProperty, mapFromPropertySource.get(keyProperty).toString());
    }

    private static List<MapPropertySource> getMapPropertySources(AbstractEnvironment environment) {
        List<MapPropertySource> propertySources = new ArrayList<>();
        for (Iterator<PropertySource<?>> it = environment.getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource<?> propertySource = it.next();
            if (propertySource instanceof MapPropertySource) {
                propertySources.add(0, (MapPropertySource) propertySource);
            }
        }
        return propertySources;
    }

    private static void bindProperties(Map<String, PropertySourceOriginValue> properties, ObjectWithMeta objectWithMeta) {
        TypeMetadata objectMetadata = objectWithMeta.getObjectMetadata();
        Object realObject = objectWithMeta.getRealObject();
        for (Entry<String, PropertySourceOriginValue> propertyEntry : properties.entrySet()) {
            String propertyKey = propertyEntry.getKey();
            String fieldName = propertyKey;
            if (propertyKey.contains("-")) {
                fieldName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, propertyKey);
            }

            fieldName = fieldName.split("\\[")[0];

            String setterName = buildSetterMethodName(fieldName);
            TypeMetadata metaForField = getMetaForField(objectMetadata, fieldName, propertyEntry.getValue());
            Method[] methods = objectMetadata.getRawType().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(setterName)) {
                    List<Class<?>> methodArgsTypes = Elements.elements(method.getParameterTypes()).asList();
                    if (methodArgsTypes.size() == 1) {
                        Class<?> setterArgument = methodArgsTypes.get(0);
                        if (metaForField.getRawType().isAssignableFrom(setterArgument)) {
                            Object newObject = getNodeValue(propertyEntry, realObject, TypeMetadataForField.create(fieldName, metaForField));
                            if (newObject != null) {
                                try {
                                    method.invoke(realObject, newObject);
                                } catch (ReflectiveOperationException e) {
                                    // nop
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static TypeMetadata getMetaForField(TypeMetadata objectMetadata, String fieldName, PropertySourceOriginValue propertySourceOriginValue) {
        try {
            return objectMetadata.getMetaForField(fieldName);
        } catch (ReflectionOperationException ex) {
            throw new IllegalArgumentException(String.format("cannot find field '%s' in class %s, %s",
                fieldName, objectMetadata.getRawType().getCanonicalName(), problematicKeyValue(propertySourceOriginValue)), ex);
        }
    }

    private static Object getNodeValue(Entry<String, PropertySourceOriginValue> propertyEntry, Object parentNode, TypeMetadataForField typeMetadataForField) {
        TypeMetadata metaForField = typeMetadataForField.getMetaForField();
        Object newObject = null;
        Class<?> rawFieldType = metaForField.getRawType();
        PropertySourceOriginValue propertySourceOriginValue = propertyEntry.getValue();
        Object propertyValue = propertySourceOriginValue.getPropertyValue();

        if (metaForField.isSimpleType()) {
            String rawValueAsText = (String) propertyValue;
            Function<String, Object> stringObjectFunction = OBJECT_FACTORY_BY_CLASS_TYPE.get(rawFieldType);
            if (stringObjectFunction != null) {
                try {
                    newObject = stringObjectFunction.apply(rawValueAsText);
                } catch (Exception ex) {
                  // nop
                }
            } else {
                for (Class<?> typeFromMapConversion : OBJECT_FACTORY_BY_CLASS_TYPE.keySet()) {
                    if (rawFieldType.isAssignableFrom(typeFromMapConversion)) {
                        newObject = rawValueAsText;
                        break;
                    }
                }
            }
        } else if (metaForField.isMapType()) {
            TypeMetadata typeOfMapValue = metaForField.getGenericType(1);
            Map<String, PropertySourceOriginValue> mapFromProperties = getAsMap(propertyValue);
            Map<String, Object> newMap = new HashMap<>();

            for (Entry<String, PropertySourceOriginValue> entryFromMap : mapFromProperties.entrySet()) {
                Object newMapValue = getNodeValue(entryFromMap, parentNode, TypeMetadataForField.create(typeOfMapValue));
                newMap.put(entryFromMap.getKey(), newMapValue);
            }

            newObject = newMap;
        } else if (isCollectionType(metaForField.getRawType())) {
            int atIndexToAdd = Integer.parseInt(propertyEntry.getKey().split("\\[")[1].replaceAll("]", ""));
            TypeMetadata typeInCollection = metaForField.getGenericType(0);
            newObject = InvokableReflectionUtils.getValueOfField(parentNode, typeMetadataForField.getFieldName());
            if (newObject == null) {
                if (rawFieldType.isAssignableFrom(List.class)) {
                    newObject = new ArrayList<>();
                } else if (rawFieldType.isAssignableFrom(Set.class)) {
                    newObject = new HashMap<>();
                } else {
                    throw new IllegalStateException("Not supported type: " + metaForField.getRawType().getCanonicalName());
                }
            }
            addToCollectionAtIndex(newObject, atIndexToAdd, getNodeValue(propertyEntry, parentNode, TypeMetadataForField.create(typeInCollection)));
        } else {
            newObject = InvokableReflectionUtils.newInstance(rawFieldType);
            bindProperties(getAsMap(propertyValue),
                ObjectWithMeta.builder()
                    .realObject(newObject)
                    .objectMetadata(metaForField)
                    .build());
        }
        if (newObject == null) {
            throw new IllegalArgumentException(
                String.format("Cannot set field: '%s' with type '%s' in class %s, %s",
                    typeMetadataForField.getFieldName(), rawFieldType.getCanonicalName(), parentNode.getClass().getCanonicalName(),
                    problematicKeyValue(propertySourceOriginValue)));
        }
        return newObject;
    }

    private static Map<String, PropertySourceOriginValue> getAsMap(Object object) {
        return (Map<String, PropertySourceOriginValue>) object;
    }

    private static String problematicKeyValue(PropertySourceOriginValue propertySourceOriginValue) {
        return String.format("problematic property key: %s=%s from: %s", propertySourceOriginValue.getPropertyKey(),
            propertySourceOriginValue.getPropertyValue(), propertySourceOriginValue.getPropertyKeySource());
    }

    private static String buildSetterMethodName(String fieldName) {
        String firstChar = fieldName.substring(0, 1).toUpperCase();
        return "set" + firstChar + fieldName.substring(1);
    }

    @Data
    @Builder
    private static class ObjectWithMeta {

        private Object realObject;
        private TypeMetadata objectMetadata;
    }

    @Data
    @Builder
    private static class TypeMetadataForField {

        private String fieldName;
        private TypeMetadata metaForField;

        public static TypeMetadataForField create(TypeMetadata metaForField) {
            return create(null, metaForField);
        }

        public static TypeMetadataForField create(String fieldName, TypeMetadata metaForField) {
            return TypeMetadataForField.builder()
                .fieldName(fieldName)
                .metaForField(metaForField)
                .build();
        }
    }

    @Value
    private static class PropertySourceOriginValue {
        String propertyKeySource;
        String propertyKey;
        Object propertyValue;
    }

    private static void addToCollectionAtIndex(Object collectionObject, int index, Object objectToAdd) {
        if (collectionObject instanceof List) {
            List<Object> list = (List<Object>) collectionObject;
            if (list.size() > index) {
                list.set(index, objectToAdd);
            } else {
                for (int i = list.size(); i < index; i++) {
                    list.add(null);
                }
                list.add(objectToAdd);
            }
        } else if (collectionObject instanceof Set)  {
            ((Set<Object>) collectionObject).add(objectToAdd);
        } else {
            throw new IllegalStateException("Not supported type: " + collectionObject.getClass().getCanonicalName());
        }
    }
}

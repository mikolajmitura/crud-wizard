package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ReflectionOperationException;

@UtilityClass
class JsonPropertiesResolver {

    static List<AdditionalPropertyMetaModel> resolveJsonProperties(AccessFieldType accessFieldType, Elements<JsonProperty> jsonProperties) {
        JsonProperty jsonPropertyAnnotation = jsonProperties
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        if (jsonPropertyAnnotation != null) {
            return Elements.elements(
                AdditionalPropertyMetaModel.builder()
                    .name("@Json_Field_Name__" + accessFieldType.name())
                    .valueRealClassName(jsonPropertyAnnotation.value())
                    .build()
            ).asMutableList();
        }
        return new ArrayList<>();
    }

    static JsonProperty findJsonPropertyInField(Class<?> realClass, String fieldName) {
        try {
            Field field = MetadataReflectionUtils.getField(realClass, fieldName);
            return field.getDeclaredAnnotation(JsonProperty.class);
        } catch (ReflectionOperationException ex) {
            return null;
        }
    }
}

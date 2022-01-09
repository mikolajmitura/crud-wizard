package pl.jalokim.crudwizard.core.metamodels;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ValidatorMetaModel extends WithAdditionalPropertiesMetaModel {

    public static final String PLACEHOLDER_PREFIX = "_PLACEHOLDER_ARG_";

    Long id;

    Class<?> realClass;
    String validatorName;

    Object validatorInstance;

    String validatorScript;

    String namePlaceholder;
    String messagePlaceholder;

    public Map<String, Object> fetchMessagePlaceholders() {
        Map<String, Object> placeholders = new HashMap<>();
        for (AdditionalPropertyMetaModel additionalProperty : getAdditionalProperties()) {
            if (additionalProperty.getName().startsWith(PLACEHOLDER_PREFIX)) {
                placeholders.put(additionalProperty.getName().replaceFirst(PLACEHOLDER_PREFIX, ""),
                    additionalProperty.getRealValue()
                );
            }
        }
        return placeholders;
    }
}

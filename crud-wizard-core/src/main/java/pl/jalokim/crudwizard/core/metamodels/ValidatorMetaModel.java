package pl.jalokim.crudwizard.core.metamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorMetaModel extends AdditionalPropertyMetaModelDto {

    public static final String PLACEHOLDER_PREFIX = "_PLACEHOLDER_ARG_";

    Long id;

    Class<?> realClass;
    String validatorName;

    Object validatorInstance;

    String validatorScript;

    String namePlaceholder;
    String messagePlaceholder;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();

    public Map<String, Object> getMessagePlaceholders() {
        Map<String, Object> placeholders = new HashMap<>();
        for (AdditionalPropertyDto additionalProperty : getAdditionalProperties()) {
            if (additionalProperty.getName().startsWith(PLACEHOLDER_PREFIX)) {
                placeholders.put(additionalProperty.getName().replaceFirst(PLACEHOLDER_PREFIX, ""),
                    additionalProperty.getRealValue()
                );
            }
        }
        return placeholders;
    }
}

package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.jalokim.utils.collection.Elements;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AdditionalPropertyMetaModelDto extends ObjectWithVersion {

    protected List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();

    public Object getPropertyValue(String propertyName) {
        return Elements.elements(additionalProperties)
            .filter(property -> property.getName().equals(propertyName))
            .map(AdditionalPropertyDto::getValue)
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyRealValue(String propertyName) {
        return Elements.elements(additionalProperties)
            .filter(property -> property.getName().equals(propertyName))
            .map(property -> (T) property.getRealValue())
            .findFirst()
            .orElse(null);
    }

    public AdditionalPropertyDto getProperty(String propertyName) {
        return Elements.elements(additionalProperties)
            .filter(property -> property.getName().equals(propertyName))
            .findFirst()
            .orElse(null);
    }

    public void addProperty(String propertyName, Object value) {
        additionalProperties.add(AdditionalPropertyDto.builder()
                .name(propertyName)
                .value(value)
                .valueRealClassName(getFullClassName(value))
                .build()
        );
    }

    public void addNullProperty(String propertyName, Class<?> objectType) {
        additionalProperties.add(AdditionalPropertyDto.builder()
            .name(propertyName)
            .value(null)
            .valueRealClassName(objectType.getCanonicalName())
            .build()
        );
    }

}

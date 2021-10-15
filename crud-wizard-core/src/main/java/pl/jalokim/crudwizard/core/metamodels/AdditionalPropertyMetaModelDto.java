package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.jalokim.utils.collection.Elements;

@Data
@EqualsAndHashCode(callSuper = true)
// TODO validator for check property name uniqueness
public abstract class AdditionalPropertyMetaModelDto extends ObjectWithVersion {

    protected List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();

    public Object getPropertyValue(String propertyName) {
        return Elements.elements(additionalProperties)
            .filter(property -> property.getName().equals(propertyName))
            .map(AdditionalPropertyDto::getRealValue)
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

    @SuppressWarnings("unchecked")
    public <T> T addProperty(String propertyName, Object value) {
        additionalProperties.add(AdditionalPropertyDto.builder()
                .name(propertyName)
                .valueAsObject(value)
                .valueRealClassName(getFullClassName(value))
                .build()
        );
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T> T updateProperty(String propertyName, Object value) {
        for (int index = 0; index < additionalProperties.size(); index++) {
            AdditionalPropertyDto additionalPropertyDto = additionalProperties.get(index);
            if (additionalPropertyDto.getName().equals(propertyName)) {
                additionalProperties.remove(index);
                break;
            }
        }
        return addProperty(propertyName, value);
    }
}

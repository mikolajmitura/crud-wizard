package pl.jalokim.crudwizard.core.metamodels;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;

import java.util.List;
import javax.validation.Valid;
import lombok.EqualsAndHashCode;
import pl.jalokim.utils.collection.Elements;

@EqualsAndHashCode(callSuper = true)
// TODO validator for check property name uniqueness
@SuppressWarnings("unchecked")
public abstract class AdditionalPropertyMetaModelDto extends ObjectWithVersion {

    public abstract List<AdditionalPropertyDto> getAdditionalProperties();

    public abstract void setAdditionalProperties(List<@Valid AdditionalPropertyDto> additionalProperties);

    public Object getPropertyValue(String propertyName) {
        return Elements.elements(getAdditionalProperties())
            .filter(property -> property.getName().equals(propertyName))
            .map(AdditionalPropertyDto::getRealValue)
            .findFirst()
            .orElse(null);
    }

    public <T> T getPropertyRealValue(String propertyName) {
        return Elements.elements(getAdditionalProperties())
            .filter(property -> property.getName().equals(propertyName))
            .map(property -> (T) property.getRealValue())
            .findFirst()
            .orElse(null);
    }

    public AdditionalPropertyDto getProperty(String propertyName) {
        return Elements.elements(getAdditionalProperties())
            .filter(property -> property.getName().equals(propertyName))
            .findFirst()
            .orElse(null);
    }

    public <T> T addProperty(String propertyName, Object value) {
        getAdditionalProperties().add(AdditionalPropertyDto.builder()
            .name(propertyName)
            .valueAsObject(value)
            .valueRealClassName(getFullClassName(value))
            .build()
        );
        return (T) this;
    }

    public <T> T addAllAdditionalProperties(List<AdditionalPropertyDto> additionalProperties) {
        this.getAdditionalProperties().addAll(additionalProperties);
        return (T) this;
    }

    public <T> T updateProperty(String propertyName, Object value) {
        for (int index = 0; index < getAdditionalProperties().size(); index++) {
            AdditionalPropertyDto additionalPropertyDto = getAdditionalProperties().get(index);
            if (additionalPropertyDto.getName().equals(propertyName)) {
                getAdditionalProperties().remove(index);
                break;
            }
        }
        return addProperty(propertyName, value);
    }
}

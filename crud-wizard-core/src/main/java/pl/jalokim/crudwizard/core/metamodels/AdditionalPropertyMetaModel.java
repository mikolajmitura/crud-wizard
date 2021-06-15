package pl.jalokim.crudwizard.core.metamodels;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.jalokim.utils.collection.Elements;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AdditionalPropertyMetaModel extends BaseModelDto {

    protected List<AdditionalPropertyDto> additionalProperties;

    public Object getPropertyValue(String propertyName) {
        return Elements.elements(additionalProperties)
            .filter(property -> property.getName().equals(propertyName))
            .map(AdditionalPropertyDto::getValue)
            .findFirst()
            .orElse(null);
    }

    public AdditionalPropertyDto getProperty(String propertyName) {
        return Elements.elements(additionalProperties)
            .filter(property -> property.getName().equals(propertyName))
            .findFirst()
            .orElse(null);
    }

}

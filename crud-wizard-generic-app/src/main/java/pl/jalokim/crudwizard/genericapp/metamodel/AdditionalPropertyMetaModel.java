package pl.jalokim.crudwizard.genericapp.metamodel;

import java.util.List;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.metamodel.properties.AdditionalPropertyDto;
import pl.jalokim.utils.collection.Elements;

@Data
public class AdditionalPropertyMetaModel {

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

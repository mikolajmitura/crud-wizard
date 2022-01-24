package pl.jalokim.crudwizard.genericapp.mapper.generete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.PropertyValueMappingStrategy;

@Value
@Builder
public class PropertiesOverriddenMapping {

    public static final String CURRENT_NODE = "$";

    String propertyName;
    List<PropertyValueMappingStrategy> valueMappingStrategy;

    Map<String, PropertiesOverriddenMapping> mappingsByPropertyName = new HashMap<>();

}

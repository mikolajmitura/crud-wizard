package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Data
@Builder
public class PropertiesOverriddenMapping {

    /**
     * ignoring target field names. List of field names.
     */
    @Builder.Default
    private List<String> ignoredFields = new ArrayList<>();

    /**
     * by default disabled, should inform when have problem with some field, when cannot find conversion strategy for given field types.
     */
    private boolean ignoreMappingProblem;

    @Builder.Default
    private List<ValueToAssignExpression> valueMappingStrategy = new ArrayList<>();

    @Builder.Default
    private Map<String, PropertiesOverriddenMapping> mappingsByPropertyName = new HashMap<>();

    public static boolean containsNestedMappings(PropertiesOverriddenMapping propertyOverriddenMapping) {
        if (propertyOverriddenMapping == null) {
            return false;
        }
        return isNotEmpty(propertyOverriddenMapping.getValueMappingStrategy()) ||
            !propertyOverriddenMapping.getMappingsByPropertyName().isEmpty();
    }

    public static List<ValueToAssignExpression> findOverriddenMappingStrategiesForCurrentNode(PropertiesOverriddenMapping propertiesOverriddenMapping) {
        return Optional.ofNullable(propertiesOverriddenMapping)
            .map(PropertiesOverriddenMapping::getValueMappingStrategy)
            .orElse(List.of());
    }

    public static List<ValueToAssignExpression> findOverriddenMappingStrategies(PropertiesOverriddenMapping propertyOverriddenMapping, String forField) {
        return Optional.ofNullable(propertyOverriddenMapping)
            .map(PropertiesOverriddenMapping::getMappingsByPropertyName)
            .map(propertiesMapping -> propertiesMapping.get(forField))
            .map(PropertiesOverriddenMapping::getValueMappingStrategy)
            .orElse(List.of());
    }

    public static boolean givenFieldIsIgnored(PropertiesOverriddenMapping propertyOverriddenMapping, String fieldName) {
        return Optional.ofNullable(propertyOverriddenMapping)
            .map(mappingLevel -> mappingLevel.getIgnoredFields().contains(fieldName))
            .orElse(false);
    }

    public static PropertiesOverriddenMapping getPropertiesOverriddenMapping(PropertiesOverriddenMapping propertyOverriddenMapping, String forField) {
        return Optional.ofNullable(propertyOverriddenMapping)
            .map(PropertiesOverriddenMapping::getMappingsByPropertyName)
            .map(propertiesMapping -> propertiesMapping.get(forField))
            .orElseGet(() -> PropertiesOverriddenMapping.builder().build());
    }

}

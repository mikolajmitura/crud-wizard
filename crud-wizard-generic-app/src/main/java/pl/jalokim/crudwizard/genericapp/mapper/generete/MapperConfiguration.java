package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.WRITE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.PropertyValueMappingStrategy;

@Value
@Builder
public class MapperConfiguration {

    /**
     * disable auto mapping, by default is enabled.
     */
    // TODO #1 what means auto mapping?
    @Builder.Default
    boolean disableAutoMapping = true;

    /**
     * ignoring target field names. List of field names.
     */
    @Builder.Default
    List<String> ignoredFields = new ArrayList<>();

    /**
     * by default disabled, should inform when have problem with some field, when cannot find conversion strategy for given field types.
     */
    @Builder.Default
    boolean ignoreMappingProblem = false;

    @Builder.Default
    FieldMetaResolverConfiguration fieldMetaResolverForRawTargetDto = new FieldMetaResolverConfiguration(WRITE);

    @Builder.Default
    FieldMetaResolverConfiguration fieldMetaResolverForRawSourceDto = new FieldMetaResolverConfiguration(READ);

    PropertiesOverriddenMapping propertyOverriddenMapping;

    public List<PropertyValueMappingStrategy> findOverriddenMappingStrategiesForCurrentNode() {
        return findOverriddenMappingStrategies(PropertiesOverriddenMapping.CURRENT_NODE);
    }

    public List<PropertyValueMappingStrategy> findOverriddenMappingStrategies(String forField) {
        return Optional.ofNullable(propertyOverriddenMapping)
            .map(PropertiesOverriddenMapping::getMappingsByPropertyName)
            .map(propertiesMapping -> propertiesMapping.get(forField))
            .map(PropertiesOverriddenMapping::getValueMappingStrategy)
            .orElse(List.of());
    }
}

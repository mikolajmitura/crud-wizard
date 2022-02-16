package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.WRITE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Value
@Builder(toBuilder = true)
public class MapperConfiguration {

    ClassMetaModel sourceMetaModel;
    ClassMetaModel targetMetaModel;

    /**
     * disable auto mapping, by default is enabled.
     */
    // TODO #1 what does auto mapping mean?
    @Builder.Default
    boolean disableAutoMapping = true;

    /**
     * by default disabled, should inform when have problem with some field, when cannot find conversion strategy for given field types.
     */
    @Builder.Default
    boolean globalIgnoreMappingProblems = false;

    @Builder.Default
    FieldMetaResolverConfiguration fieldMetaResolverForRawTargetDto = new FieldMetaResolverConfiguration(WRITE);

    @Builder.Default
    FieldMetaResolverConfiguration fieldMetaResolverForRawSourceDto = new FieldMetaResolverConfiguration(READ);

    @Builder.Default
    PropertiesOverriddenMapping propertyOverriddenMapping = PropertiesOverriddenMapping.builder().build();

    @Builder.Default
    List<MapperConfiguration> subMappers = new ArrayList<>();

    public List<ValueToAssignExpression> findOverriddenMappingStrategiesForCurrentNode() {
        return Optional.ofNullable(propertyOverriddenMapping)
            .map(PropertiesOverriddenMapping::getValueMappingStrategy)
            .orElse(List.of());
    }

    public List<ValueToAssignExpression> findOverriddenMappingStrategies(String forField) {
        return Optional.ofNullable(propertyOverriddenMapping)
            .map(PropertiesOverriddenMapping::getMappingsByPropertyName)
            .map(propertiesMapping -> propertiesMapping.get(forField))
            .map(PropertiesOverriddenMapping::getValueMappingStrategy)
            .orElse(List.of());
    }

    public boolean givenFieldIsIgnored(String fieldName) {
        return Optional.ofNullable(getPropertyOverriddenMapping())
            .map(mappingLevel -> mappingLevel.getIgnoredFields().contains(fieldName))
            .orElse(false);
    }
}

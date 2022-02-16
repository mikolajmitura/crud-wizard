package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Value
@Builder
public class PropertiesOverriddenMapping {

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
    List<ValueToAssignExpression> valueMappingStrategy = new ArrayList<>();

    Map<String, PropertiesOverriddenMapping> mappingsByPropertyName = new HashMap<>();

}

package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PropertiesOverriddenMappingDto {

    Long id;

    @NotNull
    @Size(min = 1, max = 250)
    String targetAssignPath;

    @Size(max = 100)
    String sourceAssignExpression;

    @Builder.Default
    boolean ignoreField = false;

    @Builder.Default
    boolean ignoredAllMappingProblem = false;
}

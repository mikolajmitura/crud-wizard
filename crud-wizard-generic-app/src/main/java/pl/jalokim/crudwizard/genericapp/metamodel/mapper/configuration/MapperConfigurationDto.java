package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation.TargetAssignPaths;

@Value
@Builder(toBuilder = true)
@TargetAssignPaths
public class MapperConfigurationDto {

    Long id;

    /**
     * Mapper name or method name
     */
    @NotEmpty
    @Size(min = 3, max = 100)
    String name;

    @Valid
    @NotNull
    ClassMetaModelDto sourceMetaModel;

    @Valid
    @NotNull
    ClassMetaModelDto targetMetaModel;

    @Builder.Default
    Boolean enableAutoMapping = true;

    @Builder.Default
    Boolean ignoreMappingProblems = false;

    // TODO #1 #validation ##_3 sourceAssignExpression field will be validated through PropertiesOverriddenMappingResolver
    //  (parsing expressions and validate existence of things in parsed expression) invoked by javax validator
    List<@Valid PropertiesOverriddenMappingDto> propertyOverriddenMapping;

    @Builder.Default
    EnumEntriesMappingDto enumEntriesMapping = EnumEntriesMappingDto.builder().build();
}

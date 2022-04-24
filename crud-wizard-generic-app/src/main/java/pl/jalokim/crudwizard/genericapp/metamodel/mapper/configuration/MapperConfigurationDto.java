package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;

@Value
@Builder(toBuilder = true)
public class MapperConfigurationDto {

    Long id;

    /**
     * Mapper name or method name
     */
    // TODO #1 create validator for unique of method name or mapper name
        // use below label mapper.parser.not.unique.method.name
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

    // TODO #1 validate correctness of targetAssignPath in PropertiesOverriddenMappingDto via javax validator
    //  sourceAssignExpression field will be validated through PropertiesOverriddenMappingResolver
    List<@Valid PropertiesOverriddenMappingDto> propertyOverriddenMapping;
}

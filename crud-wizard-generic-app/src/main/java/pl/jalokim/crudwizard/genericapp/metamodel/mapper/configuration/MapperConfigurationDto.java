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
    @NotEmpty
    @Size(min = 3, max = 100)
    String name;

    @Valid
    @NotNull
    // TODO #1 maybe better will that when
    //  sourceMetaModel and targetMetaModel will be not provided by front but before validation
    //  will be attached to mapper config when post, get mapper persist etc...
    //  only will be provided by front for inner method
    ClassMetaModelDto sourceMetaModel;

    @Valid
    @NotNull
    ClassMetaModelDto targetMetaModel;

    @Builder.Default
    Boolean enableAutoMapping = true;

    @Builder.Default
    Boolean ignoreMappingProblems = false;

    List<@Valid PropertiesOverriddenMappingDto> propertyOverriddenMapping;

    @Builder.Default
    EnumEntriesMappingDto enumEntriesMapping = EnumEntriesMappingDto.builder().build();
}

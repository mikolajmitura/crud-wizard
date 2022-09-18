package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation.MapperGenerateConfigCheck;

@Value
@Builder(toBuilder = true)
@MapperGenerateConfigCheck
public class MapperGenerateConfigurationDto {

    Long id;

    @NotNull
    @Builder.Default
    Boolean globalEnableAutoMapping = true;

    @NotNull
    @Builder.Default
    Boolean globalIgnoreMappingProblems = false;

    @Valid
    FieldMetaResolverConfigurationDto fieldMetaResolverForRawTarget;

    @Valid
    FieldMetaResolverConfigurationDto fieldMetaResolverForRawSource;

    @Valid
    @NotNull
    MapperConfigurationDto rootConfiguration;

    List<@Valid MapperConfigurationDto> subMappersAsMethods;
}

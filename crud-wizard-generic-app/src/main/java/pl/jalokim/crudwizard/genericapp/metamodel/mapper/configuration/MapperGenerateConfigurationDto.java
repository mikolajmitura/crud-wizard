package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.WRITE;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto.createResolverConfigurationWith;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class MapperGenerateConfigurationDto {

    Long id;

    @NotNull
    @Builder.Default
    Boolean globalEnableAutoMapping = true;

    @NotNull
    @Builder.Default
    Boolean globalIgnoreMappingProblems = false;

    @Valid
    @Builder.Default
    FieldMetaResolverConfigurationDto fieldMetaResolverForRawTarget = createResolverConfigurationWith(WRITE);

    @Valid
    @Builder.Default
    FieldMetaResolverConfigurationDto fieldMetaResolverForRawSource = createResolverConfigurationWith(READ);

    @Valid
    @NotNull
    MapperConfigurationDto rootConfiguration;

    List<@Valid MapperConfigurationDto> subMappersAsMethods;
}

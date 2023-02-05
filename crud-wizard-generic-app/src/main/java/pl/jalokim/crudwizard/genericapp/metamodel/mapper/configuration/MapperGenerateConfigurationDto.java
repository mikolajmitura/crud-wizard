package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto.DEFAULT_CONFIG_DTO;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.compiler.CompiledCodeMetadataDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    FieldMetaResolverConfigurationDto fieldMetaResolverForRawTarget = DEFAULT_CONFIG_DTO;

    @Valid
    @Builder.Default
    FieldMetaResolverConfigurationDto fieldMetaResolverForRawSource = DEFAULT_CONFIG_DTO;

    @Valid
    @NotNull
    MapperConfigurationDto rootConfiguration;

    List<@Valid MapperConfigurationDto> subMappersAsMethods;

    /**
     * This is assigned automatically from given endpoint in BeforeEndpointValidatorUpdater
     */
    ClassMetaModelDto pathVariablesClassModel;

    /**
     * This is assigned automatically from given endpoint in BeforeEndpointValidatorUpdater
     */
    ClassMetaModelDto requestParamsClassModel;

    /**
     * This is set in MapperGenerateConfigValidator
     */
    CompiledCodeMetadataDto mapperCompiledCodeMetadata;
}

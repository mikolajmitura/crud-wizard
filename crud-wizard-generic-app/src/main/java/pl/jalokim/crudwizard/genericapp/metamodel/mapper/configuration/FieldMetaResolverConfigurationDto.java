package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverStrategyType;

@Value
@Builder
public class FieldMetaResolverConfigurationDto {

    Long id;

    @NotNull
    FieldMetaResolverStrategyType fieldMetaResolverStrategyType;

    List<@Valid FieldMetaResolverForClassEntryDto> fieldMetaResolverForClass;

    public static FieldMetaResolverConfigurationDto createResolverConfigurationWith(FieldMetaResolverStrategyType fieldMetaResolverStrategyType) {
        return FieldMetaResolverConfigurationDto.builder()
            .fieldMetaResolverStrategyType(fieldMetaResolverStrategyType)
            .build();
    }
}

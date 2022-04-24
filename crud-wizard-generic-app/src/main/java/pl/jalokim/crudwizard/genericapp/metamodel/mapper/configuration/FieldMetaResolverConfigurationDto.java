package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType;

@Value
@Builder
public class FieldMetaResolverConfigurationDto {

    Long id;

    @NotNull
    FieldMetaResolverStrategyType fieldMetaResolverStrategyType;

    List<@Valid FieldMetaResolverForClassEntryDto> fieldMetaResolverForClass;
}

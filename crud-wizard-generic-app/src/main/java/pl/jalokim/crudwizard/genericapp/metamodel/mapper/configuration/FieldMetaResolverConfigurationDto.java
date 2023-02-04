package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.validation.Valid;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FieldMetaResolverConfigurationDto {

    public static final FieldMetaResolverConfigurationDto DEFAULT_CONFIG_DTO = FieldMetaResolverConfigurationDto.builder().build();

    Long id;

    List<@Valid WriteFieldMetaResolverForClassEntryDto> writeFieldMetaResolverForClass;
    List<@Valid ReadFieldMetaResolverForClassEntryDto> readFieldMetaResolverForClass;
}

package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyDto;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointResponseMetaModelDto extends WithAdditionalPropertiesDto {

    Long id;
    @Valid
    ClassMetaModelDto classMetaModel;

    @Min(100)
    @Max(599)
    Long successHttpCode;

    @Valid
    MapperMetaModelDto mapperMetaModel;

    @Valid
    QueryProviderDto queryProvider;

    @Builder.Default
    List<@Valid AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}

package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EndpointResponseMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    @Valid
    ClassMetaModelDto classMetaModel;
    @Min(value = 100)
    @Max(value = 599)
    Long successHttpCode;
}

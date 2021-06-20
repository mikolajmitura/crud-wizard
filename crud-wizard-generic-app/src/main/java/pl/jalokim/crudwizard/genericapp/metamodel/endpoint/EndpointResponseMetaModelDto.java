package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

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
    ClassMetaModelDto classMetaModel;
    Long successHttpCode;
}

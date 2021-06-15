package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EndpointResponseMetaModel extends AdditionalPropertyMetaModel {

    Long id;
    ClassMetaModel classMetaModel;
    Long successHttpCode;
}

package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class EndpointResponseMetaModel extends AdditionalPropertyMetaModel {

    Long id;
    ClassMetaModel classMetaModel;
    Long successHttpCode;
}

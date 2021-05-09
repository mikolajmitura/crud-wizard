package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classes.ClassMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class EndpointResponseMetaModel extends ParentMetaModel {

    Long id;
    ClassMetaModel classMetaModel;
    Long successHttpCode;
}

package pl.jalokim.crudwizard.maintenance.endpoints;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classes.ClassMetaModel;

@Value
public class EndpointResponseMetaModel {

    ClassMetaModel classMetaModel;
    Long successHttpCode;
}

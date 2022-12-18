package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import lombok.Builder;
import lombok.Getter;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Builder
@Getter
public class EndpointQueryAndUrlMetaModel {

    ClassMetaModel queryArgumentsModel;
    ClassMetaModel pathParamsModel;
}

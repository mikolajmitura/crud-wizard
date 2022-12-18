package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;

public interface ClassMetaModelsPredicate {

    default boolean test(MethodArgumentMetaModel methodArgumentMetaModel,
        ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel inputTypeOfMapperOrService,
        EndpointQueryAndUrlMetaModel endpointQueryAndUrlMetaModel) {
        return inputTypeOfMapperOrService != null && innerTest(methodArgumentMetaModel, methodArgClassMetaModel,
            inputTypeOfMapperOrService, endpointQueryAndUrlMetaModel);
    }

    boolean innerTest(MethodArgumentMetaModel methodArgumentMetaModel,
        ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel inputTypeOfMapperOrService,
        EndpointQueryAndUrlMetaModel endpointQueryAndUrlMetaModel);
}

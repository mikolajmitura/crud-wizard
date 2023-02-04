package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public interface ReadFieldResolver {

    void resolveReadFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration);
}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public interface WriteFieldResolver {

    void resolveWriteFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration);
}

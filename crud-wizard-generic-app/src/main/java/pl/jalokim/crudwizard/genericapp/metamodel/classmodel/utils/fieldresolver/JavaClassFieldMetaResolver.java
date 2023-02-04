package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public class JavaClassFieldMetaResolver implements WriteFieldResolver, ReadFieldResolver {

    public static final JavaClassFieldMetaResolver INSTANCE = new JavaClassFieldMetaResolver();

    @Override
    public void resolveReadFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {

    }

    @Override
    public void resolveWriteFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {

    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import java.util.List;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.TypeMetadata;

public class JavaClassFieldMetaResolver implements FieldMetaResolver {

    public static final JavaClassFieldMetaResolver INSTANCE = new JavaClassFieldMetaResolver();

    @Override
    public List<FieldMetaModel> findFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return List.of();
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return List.of();
    }
}

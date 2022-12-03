package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import java.util.List;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.TypeMetadata;

public interface FieldMetaResolver {

    List<FieldMetaModel> findDeclaredFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration);

    List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel);
}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import java.util.List;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.TypeMetadata;

public interface FieldMetaResolver {

    List<FieldMetaModel> findFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration);

    // TODO #62 filter fields for write and write_read or maybe this method will not be necessary so then ClassMetaModel will have method for that
    List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel);
}

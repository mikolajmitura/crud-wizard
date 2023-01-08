package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType.WRITE_READ;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Field;
import java.util.List;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByDeclaredFieldsResolver implements FieldMetaResolver {

    public static final ByDeclaredFieldsResolver INSTANCE = new ByDeclaredFieldsResolver();

    @Override
    public List<FieldMetaModel> findFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return elements(elements(typeMetadata.getRawType().getDeclaredFields())
            .filter(MetadataReflectionUtils::isNotStaticField)
            .filter(this::isNotGroovyMetaClass)
            .map(field -> resolveFieldMetaModelByField(field, typeMetadata, fieldMetaResolverConfiguration)))
            .asList();
    }

    private boolean isNotGroovyMetaClass(Field field) {
        return !"groovy.lang.MetaClass".equals(field.getType().getCanonicalName());
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return classMetaModel.fetchAllFields();
    }

    public FieldMetaModel resolveFieldMetaModelByField(Field field, TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return FieldMetaModel.builder()
            .fieldName(field.getName())
            .accessFieldType(WRITE_READ)
            .fieldType(createClassMetaModel(typeMetadata.getMetaForField(field), fieldMetaResolverConfiguration))
            .build();
    }
}

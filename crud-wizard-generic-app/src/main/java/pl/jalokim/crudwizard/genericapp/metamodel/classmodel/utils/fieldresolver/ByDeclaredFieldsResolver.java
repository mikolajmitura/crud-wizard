package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Field;
import java.util.List;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByDeclaredFieldsResolver implements FieldMetaResolver {

    public static ByDeclaredFieldsResolver INSTANCE = new ByDeclaredFieldsResolver();

    @Override
    public List<FieldMetaModel> findDeclaredFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return elements(elements(typeMetadata.getRawType().getDeclaredFields())
            .filter(MetadataReflectionUtils::isNotStaticField)
            .filter(field -> isNotGroovyMetaClass(field))
            .map(field -> resolveFieldMetaModelByField(field, typeMetadata, fieldMetaResolverConfiguration)))
            .asList();
    }

    private boolean isNotGroovyMetaClass(Field field) {
        return !field.getType().getCanonicalName().equals("groovy.lang.MetaClass");
    }

    @Override
    public List<FieldMetaModel> getAllAvailableFieldsForWrite(ClassMetaModel classMetaModel) {
        return classMetaModel.fetchAllFields();
    }

    public FieldMetaModel resolveFieldMetaModelByField(Field field, TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return FieldMetaModel.builder()
            .fieldName(field.getName())
            .fieldType(createNotGenericClassMetaModel(typeMetadata.getMetaForField(field), fieldMetaResolverConfiguration, field.getName(), typeMetadata))
            .build();
    }


}

package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Field;
import java.util.List;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ByDeclaredFieldsResolver implements WriteFieldResolver, ReadFieldResolver {

    public static final ByDeclaredFieldsResolver INSTANCE = new ByDeclaredFieldsResolver();

    @Override
    public void resolveReadFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        classMetaModel.getExtendsFromModels()
            .forEach(extendsFromModel -> resolveReadFields(extendsFromModel, fieldMetaResolverConfiguration));
        classMetaModel.mergeFields(findFields(classMetaModel.getTypeMetadata(), fieldMetaResolverConfiguration, AccessFieldType.READ));
    }

    @Override
    public void resolveWriteFields(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        classMetaModel.getExtendsFromModels()
            .forEach(extendsFromModel -> resolveWriteFields(extendsFromModel, fieldMetaResolverConfiguration));
        classMetaModel.mergeFields(findFields(classMetaModel.getTypeMetadata(), fieldMetaResolverConfiguration, AccessFieldType.WRITE));
    }

    private List<FieldMetaModel> findFields(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration,
        AccessFieldType accessFieldType) {
        return elements(elements(typeMetadata.getRawType().getDeclaredFields())
            .filter(MetadataReflectionUtils::isNotStaticField)
            .filter(this::isNotGroovyMetaClass)
            .map(field -> resolveFieldMetaModelByField(field, typeMetadata, fieldMetaResolverConfiguration, accessFieldType)))
            .asList();
    }

    private boolean isNotGroovyMetaClass(Field field) {
        return !"groovy.lang.MetaClass".equals(field.getType().getCanonicalName());
    }

    public FieldMetaModel resolveFieldMetaModelByField(Field field, TypeMetadata typeMetadata,
        FieldMetaResolverConfiguration fieldMetaResolverConfiguration, AccessFieldType accessFieldType) {
        return FieldMetaModel.builder()
            .fieldName(field.getName())
            .accessFieldType(accessFieldType)
            .fieldType(createClassMetaModel(typeMetadata.getMetaForField(field), fieldMetaResolverConfiguration))
            .build();
    }
}

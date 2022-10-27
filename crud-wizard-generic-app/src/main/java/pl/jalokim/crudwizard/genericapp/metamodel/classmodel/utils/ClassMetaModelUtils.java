package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;

import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ClassMetaModelUtils {

    public static FieldMetaModel getRequiredFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return createNotGenericClassMetaModel(genericClassMetaModel, fieldMetaResolverConfig)
            .getRequiredFieldByName(fieldName);
    }

    public static FieldMetaModel getFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return createNotGenericClassMetaModel(genericClassMetaModel, fieldMetaResolverConfig)
            .getFieldByName(fieldName);
    }

    public static ClassMetaModel classMetaModelFromType(JavaTypeMetaModel javaTypeMetaModel) {
        TypeMetadata typeMetadata;
        if (javaTypeMetaModel.getOriginalType() != null) {
            typeMetadata = MetadataReflectionUtils.getTypeMetadataFromType(javaTypeMetaModel.getOriginalType());
        } else {
            typeMetadata = MetadataReflectionUtils.getTypeMetadataFromClass(javaTypeMetaModel.getRawClass());
        }
        return ClassMetaModelFactory.createClassMetaModelFor(typeMetadata, READ_FIELD_RESOLVER_CONFIG, false);
    }
}

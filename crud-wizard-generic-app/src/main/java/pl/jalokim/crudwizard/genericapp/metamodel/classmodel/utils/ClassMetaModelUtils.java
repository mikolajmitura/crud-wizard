package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.newClassMetaModelOrTheSame;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType;

import com.fasterxml.jackson.databind.JavaType;
import java.lang.reflect.Type;
import java.util.List;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;

@UtilityClass
public class ClassMetaModelUtils {

    public static FieldMetaModel getRequiredFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return newClassMetaModelOrTheSame(genericClassMetaModel, fieldMetaResolverConfig)
            .getRequiredFieldByName(fieldName);
    }

    public static FieldMetaModel getFieldFromClassModel(ClassMetaModel genericClassMetaModel,
        String fieldName, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        return newClassMetaModelOrTheSame(genericClassMetaModel, fieldMetaResolverConfig)
            .getFieldByName(fieldName);
    }

    public static ClassMetaModel classMetaModelFromType(JavaTypeMetaModel javaTypeMetaModel) {
        TypeMetadata typeMetadata;
        if (javaTypeMetaModel.getOriginalType() == null) {
            typeMetadata = MetadataReflectionUtils.getTypeMetadataFromClass(javaTypeMetaModel.getRawClass());
        } else {
            typeMetadata = MetadataReflectionUtils.getTypeMetadataFromType(javaTypeMetaModel.getOriginalType());
        }
        return ClassMetaModelFactory.createClassMetaModel(typeMetadata, DEFAULT_FIELD_RESOLVERS_CONFIG);
    }

    public static JavaType createJacksonJavaType(ClassMetaModel classMetaModel) {
        if (classMetaModel.getRealClass() == null) {
            throw new IllegalArgumentException("not supported conversion to JavaTypeMetaModel when cannot find real class in class metamodel");
        }
        Class<?> realOrBasedClass = classMetaModel.getRealClass();
        Type type = createType(classMetaModel);
        return JsonObjectMapper.getInstance().createJavaType(type, realOrBasedClass);
    }

    public static TypeMetadata createTypeMetadata(ClassMetaModel classMetaModel) {
        Type type = new TypeNameWrapper(createType(classMetaModel));
        return getTypeMetadataFromType(type);
    }

    public static Type createType(ClassMetaModel classMetaModel) {
        Class<?> realOrBasedClass = classMetaModel.getRealClass();
        List<ClassMetaModel> genericTypes = classMetaModel.getGenericTypes();
        if (CollectionUtils.isNotEmpty(genericTypes)) {
            Type[] parameters = elements(genericTypes)
                .map(ClassMetaModelUtils::createType)
                .asArray(new Type[0]);
            return new ParameterizedTypeImpl(realOrBasedClass, parameters);
        }
        return realOrBasedClass;
    }

}

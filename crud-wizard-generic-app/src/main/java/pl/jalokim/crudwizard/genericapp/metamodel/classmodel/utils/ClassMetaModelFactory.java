package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findReadFieldMetaResolver;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findWriteFieldMetaResolver;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ReadFieldResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.WriteFieldResolver;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

@UtilityClass
public class ClassMetaModelFactory {

    private static final Map<Class<?>, Map<Set<Class<?>>, ClassMetaModel>> RESOLVED_FIELD_META_MODELS_BY_CLASS = new ConcurrentHashMap<>();

    public static ClassMetaModel fromRawClass(Class<?> rawClass) {
        return ClassMetaModel.builder()
            .className(rawClass.getCanonicalName())
            .realClass(rawClass)
            .build();
    }

    public static ClassMetaModel createClassMetaModel(Type type, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        return createClassMetaModel(getTypeMetadataFromType(type), fieldMetaResolverConfiguration);
    }

    public static ClassMetaModel createClassMetaModel(Type type) {
        return createClassMetaModel(type, FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG);
    }

    public static ClassMetaModel createClassMetaModel(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfiguration) {
        Class<?> realRawClass = typeMetadata.getRawType();
        if (realRawClass.equals(Object.class)) {
            return ClassMetaModel.builder()
                .realClass(Object.class)
                .build();
        }

        ReadFieldResolver readFieldMetaResolver = findReadFieldMetaResolver(realRawClass, fieldMetaResolverConfiguration);
        WriteFieldResolver writeFieldMetaResolver = findWriteFieldMetaResolver(realRawClass, fieldMetaResolverConfiguration);

        Set<Class<?>> fieldMetaResolversClasses = new HashSet<>();
        fieldMetaResolversClasses.add(readFieldMetaResolver.getClass());
        fieldMetaResolversClasses.add(writeFieldMetaResolver.getClass());

        if (!typeMetadata.hasGenericTypes()) {
            if (RESOLVED_FIELD_META_MODELS_BY_CLASS.containsKey(realRawClass)) {
                var resolvedByFieldMetaResolver = RESOLVED_FIELD_META_MODELS_BY_CLASS.get(realRawClass);

                if (resolvedByFieldMetaResolver.containsKey(fieldMetaResolversClasses)) {
                    return resolvedByFieldMetaResolver.get(fieldMetaResolversClasses);
                }
            }
        }

        ClassMetaModel classMetaModel = ClassMetaModel.builder().build();
        if (CollectionUtils.isNotEmpty(fieldMetaResolversClasses)) {
            storeInCache(realRawClass, fieldMetaResolversClasses, classMetaModel);
        }

        classMetaModel.setFieldMetaResolverConfiguration(fieldMetaResolverConfiguration);
        classMetaModel.setRealClass(realRawClass);
        classMetaModel.setClassName(realRawClass.getCanonicalName());

        if (typeMetadata.isSimpleType()) {
            classMetaModel.setGenericTypes(List.of());
        } else {
            classMetaModel.setGenericTypes(elements(typeMetadata.getGenericTypes())
                .map(genericType -> createClassMetaModel(genericType, fieldMetaResolverConfiguration))
                .asList());
        }

        return classMetaModel;
    }

    public static ClassMetaModel newClassMetaModelOrTheSame(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration configuration) {

        if (classMetaModel.isOnlyRawClassModel() && !Objects.equals(classMetaModel.getFieldMetaResolverConfiguration(), configuration)) {
            Type type = new TypeNameWrapper(ClassMetaModelUtils.createType(classMetaModel));
            return createClassMetaModel(type, configuration);
        }

        return classMetaModel;
    }

    private static void storeInCache(Class<?> realRawClass, Set<Class<?>> fieldMetaResolversClasses, ClassMetaModel classMetaModel) {
        Map<Set<Class<?>>, ClassMetaModel> resolvedByFieldMetaResolver;

        if (RESOLVED_FIELD_META_MODELS_BY_CLASS.containsKey(realRawClass)) {
            resolvedByFieldMetaResolver = RESOLVED_FIELD_META_MODELS_BY_CLASS.get(realRawClass);
        } else {
            resolvedByFieldMetaResolver = new ConcurrentHashMap<>();
            RESOLVED_FIELD_META_MODELS_BY_CLASS.put(realRawClass, resolvedByFieldMetaResolver);
        }

        if (!resolvedByFieldMetaResolver.containsKey(fieldMetaResolversClasses)) {
            resolvedByFieldMetaResolver.put(fieldMetaResolversClasses, classMetaModel);
        }
    }

    public static void clearCache() {
        RESOLVED_FIELD_META_MODELS_BY_CLASS.clear();
    }
}


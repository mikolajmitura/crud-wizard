package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.TypeMetadata;

@UtilityClass
public class ClassMetaModelFactory {

    private static final Map<Class<?>, Map<Set<FieldMetaResolver>, ClassMetaModel>> RESOLVED_FIELD_META_MODELS_BY_CLASS = new ConcurrentHashMap<>();

    public static ClassMetaModel fromRawClass(Class<?> rawClass) {
        return ClassMetaModel.builder()
            .className(rawClass.getCanonicalName())
            .realClass(rawClass)
            .build();
    }

    public static ClassMetaModel createClassMetaModel(Type type, FieldMetaResolverConfiguration... fieldMetaResolverConfigurations) {
        return createClassMetaModel(getTypeMetadataFromType(type), fieldMetaResolverConfigurations);
    }

    public static ClassMetaModel createClassMetaModel(TypeMetadata typeMetadata, FieldMetaResolverConfiguration... fieldMetaResolverConfigurations) {
        Class<?> realRawClass = typeMetadata.getRawType();
        if (realRawClass.equals(Object.class)) {
            return ClassMetaModel.builder()
                .realClass(Object.class)
                .build();
        }

        List<FieldMetaResolver> fieldResolversList = Elements.elements(fieldMetaResolverConfigurations)
            .map(config -> findFieldMetaResolver(realRawClass, config))
            .asList();

        Set<FieldMetaResolver> fieldMetaResolvers = elements(fieldResolversList).asSet();
        if (!typeMetadata.hasGenericTypes()) {
            if (RESOLVED_FIELD_META_MODELS_BY_CLASS.containsKey(realRawClass)) {
                var resolvedByFieldMetaResolver = RESOLVED_FIELD_META_MODELS_BY_CLASS.get(realRawClass);

                if (resolvedByFieldMetaResolver.containsKey(fieldMetaResolvers)) {
                    return resolvedByFieldMetaResolver.get(fieldMetaResolvers);
                }
            }
        }

        ClassMetaModel classMetaModel = ClassMetaModel.builder().build();
        if (CollectionUtils.isNotEmpty(fieldMetaResolvers)) {
            storeInCache(realRawClass, fieldMetaResolvers, classMetaModel);
        }

        classMetaModel.setFieldMetaResolverConfigurations(fieldMetaResolverConfigurations);
        classMetaModel.setRealClass(realRawClass);
        classMetaModel.setClassName(realRawClass.getCanonicalName());

        if (typeMetadata.isSimpleType()) {
            classMetaModel.setGenericTypes(List.of());
        } else {
            classMetaModel.setGenericTypes(elements(typeMetadata.getGenericTypes())
                .map(genericType -> createClassMetaModel(genericType, fieldMetaResolverConfigurations))
                .asList());
        }

        return classMetaModel;
    }

    // TODO #62 this method should be removed? Or used only in mapper generation
    public static ClassMetaModel createClassMetaModelWithOtherConfig(ClassMetaModel classMetaModel,
        FieldMetaResolverConfiguration... fieldMetaResolverConfigurations) {

        if (classMetaModel.isOnlyRawClassModel()) {
            Type type = new TypeNameWrapper(ClassMetaModelUtils.createType(classMetaModel));
            return createClassMetaModel(type, fieldMetaResolverConfigurations);
        }

        return classMetaModel;
    }

    private static void storeInCache(Class<?> realRawClass, Set<FieldMetaResolver> fieldMetaResolvers, ClassMetaModel classMetaModel) {
        Map<Set<FieldMetaResolver>, ClassMetaModel> resolvedByFieldMetaResolver;

        if (RESOLVED_FIELD_META_MODELS_BY_CLASS.containsKey(realRawClass)) {
            resolvedByFieldMetaResolver = RESOLVED_FIELD_META_MODELS_BY_CLASS.get(realRawClass);
        } else {
            resolvedByFieldMetaResolver = new ConcurrentHashMap<>();
            RESOLVED_FIELD_META_MODELS_BY_CLASS.put(realRawClass, resolvedByFieldMetaResolver);
        }

        if (!resolvedByFieldMetaResolver.containsKey(fieldMetaResolvers)) {
            resolvedByFieldMetaResolver.put(fieldMetaResolvers, classMetaModel);
        }
    }

    public static void clearCache() {
        RESOLVED_FIELD_META_MODELS_BY_CLASS.clear();
    }
}


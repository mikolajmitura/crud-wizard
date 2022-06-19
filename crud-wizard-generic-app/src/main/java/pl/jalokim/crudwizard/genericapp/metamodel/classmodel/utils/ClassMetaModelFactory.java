package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ClassMetaModelFactory {

    private static final Map<Class<?>, Map<FieldMetaResolver, ClassMetaModel>> RESOLVED_CLASS_META_MODELS_BY_CLASS = new ConcurrentHashMap<>();

    /**
     * It generates all ClassMetaModel instances as generic model.
     */
    public static ClassMetaModel generateGenericClassMetaModel(Class<?> rawClass, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        TypeMetadata typeMetadataFromClass = getTypeMetadataFromType(rawClass);
        return createClassMetaModelFor(typeMetadataFromClass, fieldMetaResolverConfig, null, null, true);
    }

    /**
     * It get given classMetaModel or create new instance of ClassMetaModel but with resolved fields based on classMetaModel.realClass
     */
    public static ClassMetaModel createNotGenericClassMetaModel(ClassMetaModel classMetaModel, FieldMetaResolverConfiguration fieldMetaResolverConfig) {

        if (classMetaModel.isOnlyRawClassModel()) {
            TypeMetadata typeMetadataFromClass = getTypeMetadataFromType(classMetaModel.getRealClass());
            return createClassMetaModelFor(typeMetadataFromClass, fieldMetaResolverConfig, null, null, false);
        }

        return classMetaModel;
    }

    public static ClassMetaModel createClassMetaModelFor(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfig,
        boolean createAsGenericModel) {

        return createClassMetaModelFor(typeMetadata, fieldMetaResolverConfig, null, null, createAsGenericModel);
    }

    public static ClassMetaModel createNotGenericClassMetaModel(TypeMetadata typeMetadata,
        FieldMetaResolverConfiguration fieldMetaResolverConfig, String fieldName, TypeMetadata fieldDeclaredIn) {
        return createClassMetaModelFor(typeMetadata, fieldMetaResolverConfig, fieldName, fieldDeclaredIn, false);
    }

    public static ClassMetaModel createClassMetaModelFor(TypeMetadata typeMetadata,
        FieldMetaResolverConfiguration fieldMetaResolverConfig, String fieldName,
        TypeMetadata fieldDeclaredIn, boolean createAsGenericModel) {

        Class<?> realRawClass = typeMetadata.getRawType();
        if (realRawClass.equals(Object.class)) {
            return ClassMetaModel.builder()
                .realClass(Object.class)
                .build();
        }

        FieldMetaResolver fieldMetaResolver = null;
        if (!typeMetadata.isSimpleType()) {
            fieldMetaResolver = findFieldMetaResolver(realRawClass, fieldMetaResolverConfig);
        }

        if (!typeMetadata.hasGenericTypes()) {
            if (RESOLVED_CLASS_META_MODELS_BY_CLASS.containsKey(realRawClass)) {
                var resolvedByFieldMetaResolver = RESOLVED_CLASS_META_MODELS_BY_CLASS.get(realRawClass);
                if (resolvedByFieldMetaResolver.containsKey(fieldMetaResolver)) {
                    return resolvedByFieldMetaResolver.get(fieldMetaResolver);
                }
            }
        }

        ClassMetaModel classMetaModel = ClassMetaModel.builder().build();
        if (fieldMetaResolver != null) {
            storeInCache(realRawClass, fieldMetaResolver, classMetaModel);
        }

        if (typeMetadata.isSimpleType() || typeMetadata.rawClassIsComingFromJavaApi()
            || typeMetadata.isHavingElementsType() || typeMetadata.isEnumType()) {
            classMetaModel.setRealClass(realRawClass);
            classMetaModel.setClassName(realRawClass.getCanonicalName());
        } else {
            if (createAsGenericModel) {
                classMetaModel.setBasedOnClass(realRawClass);

                String name = StringCaseUtils.firstLetterToLowerCase(realRawClass.getSimpleName());
                if (typeMetadata.hasGenericTypes()) {
                    name = name + "_" + elements(typeMetadata.getGenericTypes())
                        .map(genericType -> genericType.getRawType().getSimpleName())
                        .concat(fieldName)
                        .concat(Optional.ofNullable(fieldDeclaredIn)
                            .map(TypeMetadata::getRawClass)
                            .map(Class::getSimpleName)
                            .orElse(null))
                        .filter(Objects::nonNull)
                        .asConcatText("_");
                }

                classMetaModel.setName(name);
            } else {
                classMetaModel.setRealClass(realRawClass);
                classMetaModel.setClassName(realRawClass.getCanonicalName());
            }
        }

        if (!typeMetadata.isSimpleType() && fieldMetaResolver != null) {
            List<FieldMetaModel> fieldMetaModels = fieldMetaResolver.findDeclaredFields(typeMetadata, fieldMetaResolverConfig);

            classMetaModel.setFields(fieldMetaModels);
        }

        List<ClassMetaModel> extendsFromModels = new ArrayList<>();
        if (typeMetadata.hasParent() && !typeMetadata.rawClassIsComingFromJavaApi()) {
            ClassMetaModel classMetaModelFor = createClassMetaModelFor(typeMetadata.getParentTypeMetadata(),
                fieldMetaResolverConfig, createAsGenericModel);
            extendsFromModels.add(classMetaModelFor);
        }

        classMetaModel.setExtendsFromModels(extendsFromModels);

        if (typeMetadata.isSimpleType()) {
            classMetaModel.setGenericTypes(List.of());
        } else {
            classMetaModel.setGenericTypes(elements(typeMetadata.getGenericTypes())
                .map(genericType -> createClassMetaModelFor(genericType, fieldMetaResolverConfig, createAsGenericModel))
                .asList());
        }

        return classMetaModel;
    }

    private static void storeInCache(Class<?> realRawClass, FieldMetaResolver fieldMetaResolver, ClassMetaModel classMetaModel) {
        Map<FieldMetaResolver, ClassMetaModel> resolvedByFieldMetaResolver;

        if (RESOLVED_CLASS_META_MODELS_BY_CLASS.containsKey(realRawClass)) {
            resolvedByFieldMetaResolver = RESOLVED_CLASS_META_MODELS_BY_CLASS.get(realRawClass);
        } else {
            resolvedByFieldMetaResolver = new ConcurrentHashMap<>();
            RESOLVED_CLASS_META_MODELS_BY_CLASS.put(realRawClass, resolvedByFieldMetaResolver);
        }

        if (!resolvedByFieldMetaResolver.containsKey(fieldMetaResolver)) {
            resolvedByFieldMetaResolver.put(fieldMetaResolver, classMetaModel);
        }
    }

    public static void clearCache() {
        RESOLVED_CLASS_META_MODELS_BY_CLASS.clear();
    }
}


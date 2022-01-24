package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.utils.reflection.TypeMetadata;

public class ClassMetaModelFactory {

    private static final Map<Class<?>, Map<FieldMetaResolver, ClassMetaModel>> RESOLVED_CLASS_META_MODELS_BY_CLASS = new ConcurrentHashMap<>();

    public static ClassMetaModel resolveClassMetaModelByClass(Class<?> rawClass, FieldMetaResolverConfiguration fieldMetaResolverConfig) {
        TypeMetadata typeMetadataFromClass = getTypeMetadataFromType(rawClass);
        return createClassMetaModelFor(typeMetadataFromClass, fieldMetaResolverConfig, null, null);
    }

    public static ClassMetaModel createClassMetaModelFor(TypeMetadata typeMetadata, FieldMetaResolverConfiguration fieldMetaResolverConfig) {
        return createClassMetaModelFor(typeMetadata, fieldMetaResolverConfig, null, null);
    }

    public static ClassMetaModel createClassMetaModelFor(TypeMetadata typeMetadata,
        FieldMetaResolverConfiguration fieldMetaResolverConfig, String fieldName, TypeMetadata fieldDeclaredIn) {

        if (typeMetadata.getRawType().equals(Object.class)) {
            return ClassMetaModel.builder()
                .realClass(Object.class)
                .build();
        }

        FieldMetaResolver fieldMetaResolver = null;
        if (!typeMetadata.isSimpleType()) {
            fieldMetaResolver = findFieldMetaResolver(typeMetadata.getRawType(), fieldMetaResolverConfig);
        }

        if (!typeMetadata.hasGenericTypes()) {
            if (RESOLVED_CLASS_META_MODELS_BY_CLASS.containsKey(typeMetadata.getRawType())) {
                var resolvedByFieldMetaResolver = RESOLVED_CLASS_META_MODELS_BY_CLASS.get(typeMetadata.getRawType());
                if (resolvedByFieldMetaResolver.containsKey(fieldMetaResolver)) {
                    return resolvedByFieldMetaResolver.get(fieldMetaResolver);
                }
            }
        }

        ClassMetaModel classMetaModel = ClassMetaModel.builder().build();
        classMetaModel.setRealClass(typeMetadata.getRawType());

        if (!typeMetadata.isSimpleType()) {
            String name = StringCaseUtils.firstLetterToLowerCase(typeMetadata.getRawType().getSimpleName());
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
        }

        classMetaModel.setClassName(typeMetadata.getRawType().getCanonicalName());

        Map<FieldMetaResolver, ClassMetaModel> resolvedByFieldMetaResolver;
        if (RESOLVED_CLASS_META_MODELS_BY_CLASS.containsKey(classMetaModel.getRealClass())) {
            resolvedByFieldMetaResolver = RESOLVED_CLASS_META_MODELS_BY_CLASS.get(classMetaModel.getRealClass());
        } else {
            resolvedByFieldMetaResolver = new HashMap<>();
            RESOLVED_CLASS_META_MODELS_BY_CLASS.put(classMetaModel.getRealClass(), resolvedByFieldMetaResolver);
        }

        if (!resolvedByFieldMetaResolver.containsKey(fieldMetaResolver)) {
            resolvedByFieldMetaResolver.put(fieldMetaResolver, classMetaModel);
        }

        if (!typeMetadata.isSimpleType() && fieldMetaResolver != null) {
            List<FieldMetaModel> fieldMetaModels = fieldMetaResolver.findDeclaredFields(typeMetadata, fieldMetaResolverConfig);

            classMetaModel.setFields(fieldMetaModels);
        }

        List<ClassMetaModel> extendsFromModels = new ArrayList<>();
        if (typeMetadata.hasParent()) {
            ClassMetaModel classMetaModelFor = createClassMetaModelFor(typeMetadata.getParentTypeMetadata(), fieldMetaResolverConfig);
            extendsFromModels.add(classMetaModelFor);
        }

        classMetaModel.setExtendsFromModels(extendsFromModels);

        if (typeMetadata.isSimpleType()) {
            classMetaModel.setGenericTypes(List.of());
        } else {
            classMetaModel.setGenericTypes(elements(typeMetadata.getGenericTypes())
                .map(genericType -> createClassMetaModelFor(genericType, fieldMetaResolverConfig))
                .asList());
        }

        return classMetaModel;
    }
}

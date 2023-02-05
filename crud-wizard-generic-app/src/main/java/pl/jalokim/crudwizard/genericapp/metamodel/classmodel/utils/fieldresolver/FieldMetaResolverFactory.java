package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasBuilderMethod;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasOneConstructorMaxArgNumbers;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasOnlyDefaultConstructor;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByGettersFieldsResolver.filterGettersFromMethods;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.newInstance;

import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@UtilityClass
public class FieldMetaResolverFactory {

    private static final Map<String, ReadFieldResolver> FIELD_READ_RESOLVER_BY_CLASS_NAME = Map.of(
        ByDeclaredFieldsResolver.class.getCanonicalName(), ByDeclaredFieldsResolver.INSTANCE,
        ByGettersFieldsResolver.class.getCanonicalName(), ByGettersFieldsResolver.INSTANCE
    );

    private static final Map<String, WriteFieldResolver> FIELD_WRITE_RESOLVER_BY_CLASS_NAME = Map.of(
        ByDeclaredFieldsResolver.class.getCanonicalName(), ByDeclaredFieldsResolver.INSTANCE,
        ByBuilderFieldsResolver.class.getCanonicalName(), ByBuilderFieldsResolver.INSTANCE,
        ByAllArgsFieldsResolver.class.getCanonicalName(), ByAllArgsFieldsResolver.INSTANCE,
        BySettersFieldsResolver.class.getCanonicalName(), BySettersFieldsResolver.INSTANCE
    );

    public static ReadFieldResolver createReadMetaResolver(String className) {
        return Optional.ofNullable(FIELD_READ_RESOLVER_BY_CLASS_NAME.get(className))
            .orElseGet(() -> (ReadFieldResolver) newInstance(loadRealClass(className)));
    }

    public static WriteFieldResolver createWriteMetaResolver(String className) {
        return Optional.ofNullable(FIELD_WRITE_RESOLVER_BY_CLASS_NAME.get(className))
            .orElseGet(() -> (WriteFieldResolver) newInstance(loadRealClass(className)));
    }

    public static ReadFieldResolver findReadFieldMetaResolver(Class<?> rawClass, FieldMetaResolverConfiguration fieldMetaResolverConfig) {
        var defaultFieldMetaResolver = findDefaultReadFieldMetaResolverForClass(rawClass);

        return fieldMetaResolverConfig.getReadMetaResolverForClass(rawClass, defaultFieldMetaResolver);
    }

    public static WriteFieldResolver findWriteFieldMetaResolver(Class<?> rawClass, FieldMetaResolverConfiguration fieldMetaResolverConfig) {
        var defaultFieldMetaResolver = findDefaultWriteFieldMetaResolverForClass(rawClass);

        return fieldMetaResolverConfig.getWriteFieldMetaResolverForClass(rawClass, defaultFieldMetaResolver);
    }

    public static ReadFieldResolver findDefaultReadFieldMetaResolverForClass(Class<?> someClass) {

        if (MetadataReflectionUtils.isHavingElementsType(someClass)) {
            return JavaClassFieldMetaResolver.INSTANCE;
        }

        if (canFindAnyGetters(someClass)) {
            return ByGettersFieldsResolver.INSTANCE;
        } else {
            return ByDeclaredFieldsResolver.INSTANCE;
        }
    }

    private static boolean canFindAnyGetters(Class<?> someClass) {
        return filterGettersFromMethods(MetadataReflectionUtils.getAllNotStaticMethods(someClass))
            .findAny().isPresent();
    }

    public static WriteFieldResolver findDefaultWriteFieldMetaResolverForClass(Class<?> someClass) {

        if (MetadataReflectionUtils.isHavingElementsType(someClass)) {
            return JavaClassFieldMetaResolver.INSTANCE;
        }

        if (hasBuilderMethod(someClass)) {
            return ByBuilderFieldsResolver.INSTANCE;
        } else if (hasOnlyDefaultConstructor(someClass)) {
            return BySettersFieldsResolver.INSTANCE;
        } else {
            if (hasOneConstructorMaxArgNumbers(someClass)) {
                return ByAllArgsFieldsResolver.INSTANCE;
            }
            return ByDeclaredFieldsResolver.INSTANCE;
        }
    }
}

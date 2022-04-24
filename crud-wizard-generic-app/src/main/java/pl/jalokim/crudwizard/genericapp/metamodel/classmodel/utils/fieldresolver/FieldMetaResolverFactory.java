package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasBuilderMethod;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasOneConstructorMaxArgNumbers;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasOnlyDefaultConstructor;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.newInstance;

import java.util.Map;
import java.util.Optional;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

public class FieldMetaResolverFactory {

    private static final Map<String, FieldMetaResolver> FIELD_META_RESOLVER_BY_CLASS_NAME = Map.of(
        ByDeclaredFieldsResolver.class.getCanonicalName(), ByDeclaredFieldsResolver.INSTANCE,
        ByBuilderFieldsResolver.class.getCanonicalName(), ByBuilderFieldsResolver.INSTANCE,
        ByGettersFieldsResolver.class.getCanonicalName(), ByGettersFieldsResolver.INSTANCE,
        ByAllArgsFieldsResolver.class.getCanonicalName(), ByAllArgsFieldsResolver.INSTANCE,
        BySettersFieldsResolver.class.getCanonicalName(), BySettersFieldsResolver.INSTANCE
    );

    public static FieldMetaResolver createFieldMetaResolver(String className) {
        return Optional.ofNullable(FIELD_META_RESOLVER_BY_CLASS_NAME.get(className))
            .orElseGet(() -> (FieldMetaResolver) newInstance(loadRealClass(className)));
    }

    public static FieldMetaResolver findFieldMetaResolver(Class<?> rawClass, FieldMetaResolverConfiguration fieldMetaResolverConfig) {
        var defaultFieldMetaResolver = findFieldMetaResolverForClass(rawClass,
            fieldMetaResolverConfig.getFieldMetaResolverStrategyType());

        return fieldMetaResolverConfig.getFieldMetaResolverForClass(rawClass,
            defaultFieldMetaResolver);
    }

    public static FieldMetaResolver findFieldMetaResolverForClass(Class<?> someClass,
        FieldMetaResolverStrategyType fieldMetaResolverStrategyType) {

        if (MetadataReflectionUtils.isHavingElementsType(someClass)) {
            return ByDeclaredFieldsResolver.INSTANCE;
        }

        if (fieldMetaResolverStrategyType.equals(READ)) {
            return ByGettersFieldsResolver.INSTANCE;
        } else {
            if (hasBuilderMethod(someClass)) {
                return ByBuilderFieldsResolver.INSTANCE;
            } else if (hasOnlyDefaultConstructor(someClass)) {
                return BySettersFieldsResolver.INSTANCE;
            } else {
                if (hasOneConstructorMaxArgNumbers(someClass)) {
                    return ByAllArgsFieldsResolver.INSTANCE;
                }
                throw new TechnicalException(createMessagePlaceholder(
                    "cannot.find.field.resolver.strategy", someClass.getCanonicalName()));
            }
        }
    }

    ;
}

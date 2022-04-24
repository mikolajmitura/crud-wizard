package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.WRITE;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;

@Value
@Builder
public class FieldMetaResolverConfiguration {

    public static final FieldMetaResolverConfiguration WRITE_FIELD_RESOLVER_CONFIG = FieldMetaResolverConfiguration.builder()
        .fieldMetaResolverStrategyType(WRITE)
        .build();

    public static final FieldMetaResolverConfiguration READ_FIELD_RESOLVER_CONFIG = FieldMetaResolverConfiguration.builder()
        .fieldMetaResolverStrategyType(READ)
        .build();

    FieldMetaResolverStrategyType fieldMetaResolverStrategyType;

    @Builder.Default
    Map<Class<?>, FieldMetaResolver> fieldMetaResolverForClass = new HashMap<>();

    public FieldMetaResolver getFieldMetaResolverForClass(Class<?> rawDtoClass, FieldMetaResolver defaultFieldMetaResolver) {
        return fieldMetaResolverForClass.getOrDefault(rawDtoClass, defaultFieldMetaResolver);
    }

    public FieldMetaResolverConfiguration putFieldResolver(Class<?> someClass, FieldMetaResolver fieldMetaResolver) {
        fieldMetaResolverForClass.put(someClass, fieldMetaResolver);
        return this;
    }
}

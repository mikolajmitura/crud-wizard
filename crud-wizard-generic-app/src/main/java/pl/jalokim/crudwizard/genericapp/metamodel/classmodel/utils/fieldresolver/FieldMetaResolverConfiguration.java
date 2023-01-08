package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverStrategyType.WRITE;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;

@Value
@Builder
public class FieldMetaResolverConfiguration {


    public static final FieldMetaResolverConfiguration WRITE_FIELD_RESOLVER_CONFIG = FieldMetaResolverConfiguration.builder()
        .fieldMetaResolverStrategyType(WRITE)
        .build();

    public static final FieldMetaResolverConfiguration READ_FIELD_RESOLVER_CONFIG = FieldMetaResolverConfiguration.builder()
        .fieldMetaResolverStrategyType(READ)
        .build();

    public static FieldMetaResolverConfiguration[] DEFAULT_CONFIGURATIONS = new FieldMetaResolverConfiguration[]
        {WRITE_FIELD_RESOLVER_CONFIG, READ_FIELD_RESOLVER_CONFIG};

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

    public AccessFieldType getFieldAccessType() {
        return fieldMetaResolverStrategyType == WRITE ? AccessFieldType.WRITE : AccessFieldType.READ;
    }
}

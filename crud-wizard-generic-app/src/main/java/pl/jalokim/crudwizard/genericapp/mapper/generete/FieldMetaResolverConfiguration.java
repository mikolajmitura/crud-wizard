package pl.jalokim.crudwizard.genericapp.mapper.generete;

import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;

@Value
public class FieldMetaResolverConfiguration {

    FieldMetaResolverStrategyType fieldMetaResolverStrategyType;

    Map<Class<?>, FieldMetaResolver> fieldMetaResolverForClass = new HashMap<>();

    public FieldMetaResolver getFieldMetaResolverForClass(Class<?> rawDtoClass, FieldMetaResolver defaultFieldMetaResolver) {
        return fieldMetaResolverForClass.getOrDefault(rawDtoClass, defaultFieldMetaResolver);
    }

    public FieldMetaResolverConfiguration putFieldResolver(Class<?> someClass, FieldMetaResolver fieldMetaResolver) {
        fieldMetaResolverForClass.put(someClass, fieldMetaResolver);
        return this;
    }
}

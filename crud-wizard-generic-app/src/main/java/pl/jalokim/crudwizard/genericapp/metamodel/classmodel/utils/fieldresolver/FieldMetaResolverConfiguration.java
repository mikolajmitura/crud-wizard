package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FieldMetaResolverConfiguration {

    public static final FieldMetaResolverConfiguration DEFAULT_FIELD_RESOLVERS_CONFIG = FieldMetaResolverConfiguration.builder().build();

    @Builder.Default
    Map<Class<?>, ReadFieldResolver> readFieldMetaResolverForClass = new HashMap<>();

    @Builder.Default
    Map<Class<?>, WriteFieldResolver> writeFieldMetaResolverForClass = new HashMap<>();

    public ReadFieldResolver getReadMetaResolverForClass(Class<?> rawDtoClass, ReadFieldResolver defaultFieldMetaResolver) {
        return readFieldMetaResolverForClass.getOrDefault(rawDtoClass, defaultFieldMetaResolver);
    }

    public WriteFieldResolver getWriteFieldMetaResolverForClass(Class<?> rawDtoClass, WriteFieldResolver defaultFieldMetaResolver) {
        return writeFieldMetaResolverForClass.getOrDefault(rawDtoClass, defaultFieldMetaResolver);
    }

    public FieldMetaResolverConfiguration putReadFieldResolver(Class<?> someClass, ReadFieldResolver fieldMetaResolver) {
        readFieldMetaResolverForClass.put(someClass, fieldMetaResolver);
        return this;
    }

    public FieldMetaResolverConfiguration putWriteFieldResolver(Class<?> someClass, WriteFieldResolver fieldMetaResolver) {
        writeFieldMetaResolverForClass.put(someClass, fieldMetaResolver);
        return this;
    }
}

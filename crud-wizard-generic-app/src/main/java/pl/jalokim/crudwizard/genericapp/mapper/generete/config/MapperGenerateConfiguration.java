package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;

@Data
@Builder(toBuilder = true)
public class MapperGenerateConfiguration {

    @Builder.Default
    private boolean globalEnableAutoMapping = true;

    /**
     * by default disabled, should inform when have problem with some field, when cannot find conversion strategy for given field types.
     */
    private boolean globalIgnoreMappingProblems;

    @Builder.Default
    private FieldMetaResolverConfiguration fieldMetaResolverForRawTarget = DEFAULT_FIELD_RESOLVERS_CONFIG;

    @Builder.Default
    private FieldMetaResolverConfiguration fieldMetaResolverForRawSource = DEFAULT_FIELD_RESOLVERS_CONFIG;

    private MapperConfiguration rootConfiguration;

    private ClassMetaModel pathVariablesClassModel;
    private ClassMetaModel requestParamsClassModel;

    private final Map<String, MapperConfiguration> mapperConfigurationByMethodName = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Map<String, MapperConfiguration>>> mapperMethodCache = new ConcurrentHashMap<>();

    public void addSubMapperConfiguration(String methodName, MapperConfiguration mapperConfiguration) {
        mapperConfigurationByMethodName.put(methodName, mapperConfiguration);
        String sourceTypeDescription = mapperConfiguration.getSourceMetaModel().getTypeDescription();
        Map<String, Map<String, MapperConfiguration>> mapForSource = mapperMethodCache.computeIfAbsent(sourceTypeDescription,
            (key) -> new ConcurrentHashMap<>());
        String targetTypeDescription = mapperConfiguration.getTargetMetaModel().getTypeDescription();
        Map<String, MapperConfiguration> mapForTarget = mapForSource.computeIfAbsent(targetTypeDescription,
            (key) -> new ConcurrentHashMap<>());
        mapForTarget.put(methodName, mapperConfiguration);
    }

    public MapperConfiguration getMapperConfigurationByMethodName(String methodName) {
        return mapperConfigurationByMethodName.get(methodName);
    }
}

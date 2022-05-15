package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.WRITE_FIELD_RESOLVER_CONFIG;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;

@Data
@Builder(toBuilder = true)
public class MapperGenerateConfiguration {

    // TODO #1 implements with usage that flag  in MapperCodeGenerator
    @Builder.Default
    private boolean globalEnableAutoMapping = true;

    /**
     * by default disabled, should inform when have problem with some field, when cannot find conversion strategy for given field types.
     */
    // TODO #1 implements with usage that flag  in MapperCodeGenerator
    @Builder.Default
    private boolean globalIgnoreMappingProblems = false;

    @Builder.Default
    private FieldMetaResolverConfiguration fieldMetaResolverForRawTarget = WRITE_FIELD_RESOLVER_CONFIG;

    @Builder.Default
    private FieldMetaResolverConfiguration fieldMetaResolverForRawSource = READ_FIELD_RESOLVER_CONFIG;

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

    public boolean hasMethodName(String methodName) {
        return mapperConfigurationByMethodName.containsKey(methodName);
    }

    public MapperConfiguration getMapperConfigurationByMethodName(String methodName) {
        return mapperConfigurationByMethodName.get(methodName);
    }

    public List<MapperConfiguration> findMapperConfigurationBy(ClassMetaModel sourceClassMetaModel, ClassMetaModel targetClassMetaModel) {
        return elements(rootConfiguration)
            .concat(mapperConfigurationByMethodName.values())
            .filter(mapperConfigEntry -> mapperConfigEntry.getSourceMetaModel().isTheSameMetaModel(sourceClassMetaModel)
            && mapperConfigEntry.getTargetMetaModel().isTheSameMetaModel(targetClassMetaModel))
            .asList();
    }
}

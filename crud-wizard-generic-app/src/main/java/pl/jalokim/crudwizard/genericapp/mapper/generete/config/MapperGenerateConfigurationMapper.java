package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.replaceAllWithEmpty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto;
import pl.jalokim.utils.collection.Elements;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class MapperGenerateConfigurationMapper {

    @Autowired
    private ClassMetaModelMapper classMetaModelMapper;

    public MapperGenerateConfiguration mapConfiguration(MapperGenerateConfigurationDto mapperGenerateConfigurationDto,
        ClassMetaModel pathVariablesClassModel, ClassMetaModel requestParamsClassModel, MetaModelContext temporaryMetaModelContext) {
        var mapperGenerateConfiguration = innerMapper(mapperGenerateConfigurationDto,
            pathVariablesClassModel, requestParamsClassModel, temporaryMetaModelContext);
        mapperGenerateConfigurationDto.getSubMappersAsMethods()
            .forEach(mapperConfigurationDto -> mapperGenerateConfiguration.addSubMapperConfiguration(
                mapperConfigurationDto.getName(), mapMapperConfiguration(mapperConfigurationDto, temporaryMetaModelContext)
            ));
        return mapperGenerateConfiguration;
    }

    @Mapping(target = "rootConfiguration",
        expression = "java(mapMapperConfiguration(mapperGenerateConfigurationDto.getRootConfiguration(), temporaryMetaModelContext))")
    protected abstract MapperGenerateConfiguration innerMapper(MapperGenerateConfigurationDto mapperGenerateConfigurationDto,
        ClassMetaModel pathVariablesClassModel, ClassMetaModel requestParamsClassModel, MetaModelContext temporaryMetaModelContext);

    protected abstract FieldMetaResolverConfiguration mapFieldMetaResolverConfiguration(FieldMetaResolverConfigurationDto fieldMetaResolverConfigurationDto);

    @Mapping(target = "sourceMetaModel",
        expression = "java(toModelFromDto(mapperConfigurationDto.getSourceMetaModel(), temporaryMetaModelContext))")
    @Mapping(target = "targetMetaModel",
        expression = "java(toModelFromDto(mapperConfigurationDto.getTargetMetaModel(), temporaryMetaModelContext))")
    @Mapping(target = "enumEntriesMapping", ignore = true) // TODO #1 #mapping_by_mapstruct impl this mapping
    protected abstract MapperConfiguration mapMapperConfiguration(MapperConfigurationDto mapperConfigurationDto, MetaModelContext temporaryMetaModelContext);

    Map<Class<?>, FieldMetaResolver> mapFieldMetaResolverForClass(List<FieldMetaResolverForClassEntryDto> fieldMetaResolversForClasses) {
        return elements(fieldMetaResolversForClasses)
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createFieldMetaResolver(entry.getResolverClassName()));
    }

    PropertiesOverriddenMapping mapPropertiesOverriddenMapping(List<PropertiesOverriddenMappingDto> mappingEntries) {
        var propertiesOverriddenMapping = PropertiesOverriddenMapping.builder().build();

        for (PropertiesOverriddenMappingDto mappingEntry : mappingEntries) {
            String targetAssignPath = mappingEntry.getTargetAssignPath();

            var currentOverriddenMappingRef = new AtomicReference<>(propertiesOverriddenMapping);

            Elements.bySplitText(replaceAllWithEmpty(targetAssignPath, " "), "\\.")
                .forEachWithIndexed(element -> {
                    String fieldNameValue = element.getValue();
                    Map<String, PropertiesOverriddenMapping> mappingsByPropertyName = currentOverriddenMappingRef.get()
                        .getMappingsByPropertyName();

                    if (element.isLast() && resolveNullableBoolean(mappingEntry.isIgnoreField())) {
                        currentOverriddenMappingRef.get().getIgnoredFields().add(fieldNameValue);
                    }

                    currentOverriddenMappingRef.set(mappingsByPropertyName
                        .computeIfAbsent(fieldNameValue, (fieldName) -> PropertiesOverriddenMapping.builder().build()));

                    if (element.isLast()) {
                        currentOverriddenMappingRef.get().setIgnoreMappingProblem(mappingEntry.isIgnoredAllMappingProblem());
                    }

                });
        }

        return propertiesOverriddenMapping;
    }

    public ClassMetaModel toModelFromDto(ClassMetaModelDto classMetaModelDto, MetaModelContext temporaryMetaModelContext) {
        return classMetaModelMapper.toModelFromDto(classMetaModelDto, temporaryMetaModelContext);
    }

    private boolean resolveNullableBoolean(Boolean nullableBoolean) {
        return Optional.ofNullable(nullableBoolean)
            .orElse(false);
    }
}

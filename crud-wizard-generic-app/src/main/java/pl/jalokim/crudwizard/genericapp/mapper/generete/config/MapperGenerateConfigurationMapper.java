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
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.RawAdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingEntity;
import pl.jalokim.utils.collection.Elements;

@Mapper(config = MapperAsSpringBeanConfig.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public abstract class MapperGenerateConfigurationMapper {

    @Autowired
    private ClassMetaModelMapper classMetaModelMapper;

    @Autowired
    private RawAdditionalPropertyMapper rawAdditionalPropertyMapper;

    public AdditionalPropertyMetaModel additionalPropertyToModel(AdditionalPropertyEntity additionalPropertyEntity) {
        return rawAdditionalPropertyMapper.additionalPropertyToModel(additionalPropertyEntity);
    }

    public abstract MapperGenerateConfigurationEntity mapToEntity(MapperGenerateConfigurationDto mapperGenerateConfigurationDto);

    /**
     * Conversion from mapper configuration dto to MapperGenerateConfiguration but without parsing expressions.
     */
    public MapperGenerateConfiguration mapConfiguration(MapperGenerateConfigurationEntity mapperGenerateConfigurationEntity,
        ClassMetaModel pathVariablesClassModel, ClassMetaModel requestParamsClassModel, MetaModelContext metaModelContext) {

        var mapperGenerateConfiguration = innerMapper(mapperGenerateConfigurationEntity,
            pathVariablesClassModel, requestParamsClassModel, metaModelContext);

        elements(mapperGenerateConfigurationEntity.getSubMappersAsMethods())
            .forEach(mapperConfigurationDto -> mapperGenerateConfiguration.addSubMapperConfiguration(
                mapperConfigurationDto.getName(), mapMapperConfiguration(metaModelContext, mapperConfigurationDto)
            ));

        return mapperGenerateConfiguration;
    }

    public MapperGenerateConfiguration mapConfiguration(MapperGenerateConfigurationDto mapperGenerateConfigurationDto,
        ClassMetaModel pathVariablesClassModel, ClassMetaModel requestParamsClassModel, MetaModelContext metaModelContext) {

        var mapperGenerateConfiguration = innerMapper(mapperGenerateConfigurationDto,
            pathVariablesClassModel, requestParamsClassModel, metaModelContext);

        elements(mapperGenerateConfigurationDto.getSubMappersAsMethods())
            .forEach(mapperConfigurationDto -> mapperGenerateConfiguration.addSubMapperConfiguration(
                mapperConfigurationDto.getName(), mapMapperConfiguration(metaModelContext, mapperConfigurationDto)
            ));

        return mapperGenerateConfiguration;
    }

    @Mapping(target = "rootConfiguration",
        expression = "java(mapMapperConfiguration(metaModelContext, mapperGenerateConfigurationEntity.getRootConfiguration()))")
    @Mapping(target = "pathVariablesClassModel", source = "pathVariablesClassModel")
    @Mapping(target = "requestParamsClassModel", source = "requestParamsClassModel")
    protected abstract MapperGenerateConfiguration innerMapper(MapperGenerateConfigurationEntity mapperGenerateConfigurationEntity,
        ClassMetaModel pathVariablesClassModel, ClassMetaModel requestParamsClassModel, MetaModelContext metaModelContext);

    @Mapping(target = "rootConfiguration",
        expression = "java(mapMapperConfiguration(metaModelContext, mapperGenerateConfigurationDto.getRootConfiguration()))")
    @Mapping(target = "pathVariablesClassModel", source = "pathVariablesClassModel")
    @Mapping(target = "requestParamsClassModel", source = "requestParamsClassModel")
    protected abstract MapperGenerateConfiguration innerMapper(MapperGenerateConfigurationDto mapperGenerateConfigurationDto,
        ClassMetaModel pathVariablesClassModel, ClassMetaModel requestParamsClassModel, MetaModelContext metaModelContext);

    protected abstract FieldMetaResolverConfiguration mapFieldMetaResolverConfiguration(FieldMetaResolverConfigurationEntity fieldMetaResolverConfiguration);

    @Mapping(target = "sourceMetaModel",
        expression = "java(toModelFromEntity(metaModelContext, mapperConfigurationEntity.getSourceMetaModel()))")
    @Mapping(target = "targetMetaModel",
        expression = "java(toModelFromEntity(metaModelContext, mapperConfigurationEntity.getTargetMetaModel()))")
    protected abstract MapperConfiguration mapMapperConfiguration(MetaModelContext metaModelContext, MapperConfigurationEntity mapperConfigurationEntity);

    @Mapping(target = "sourceMetaModel",
        expression = "java(toModelFromDto(metaModelContext, mapperConfigurationDto.getSourceMetaModel()))")
    @Mapping(target = "targetMetaModel",
        expression = "java(toModelFromDto(metaModelContext, mapperConfigurationDto.getTargetMetaModel()))")
    protected abstract MapperConfiguration mapMapperConfiguration(MetaModelContext metaModelContext, MapperConfigurationDto mapperConfigurationDto);

    Map<Class<?>, FieldMetaResolver> mapFieldMetaResolverForClassFromEntity(List<FieldMetaResolverForClassEntryEntity> fieldMetaResolversForClasses) {
        return elements(fieldMetaResolversForClasses)
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createFieldMetaResolver(entry.getResolverClassName()));
    }

    Map<Class<?>, FieldMetaResolver> mapFieldMetaResolverForClassFromDto(List<FieldMetaResolverForClassEntryDto> fieldMetaResolversForClasses) {
        return elements(fieldMetaResolversForClasses)
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createFieldMetaResolver(entry.getResolverClassName()));
    }

    PropertiesOverriddenMapping mapPropertiesOverriddenMapping(List<PropertiesOverriddenMappingEntity> mappingEntries) {
        var propertiesOverriddenMapping = PropertiesOverriddenMapping.builder().build();

        for (PropertiesOverriddenMappingEntity mappingEntry : elements(mappingEntries).asList()) {
            String targetAssignPath = mappingEntry.getTargetAssignPath();

            var currentOverriddenMappingRef = new AtomicReference<>(propertiesOverriddenMapping);

            Elements.bySplitText(replaceAllWithEmpty(targetAssignPath, " "), "\\.")
                .forEachWithIndexed(element -> {
                    String fieldNameValue = element.getValue();
                    Map<String, PropertiesOverriddenMapping> mappingsByPropertyName = currentOverriddenMappingRef.get()
                        .getMappingsByPropertyName();

                    if (element.isLast() && resolveNullableBoolean(mappingEntry.getIgnoreField())) {
                        currentOverriddenMappingRef.get().getIgnoredFields().add(fieldNameValue);
                    }

                    currentOverriddenMappingRef.set(mappingsByPropertyName
                        .computeIfAbsent(fieldNameValue, (fieldName) -> PropertiesOverriddenMapping.builder().build()));

                    if (element.isLast()) {
                        currentOverriddenMappingRef.get().setIgnoreMappingProblem(mappingEntry.getIgnoredAllMappingProblem());
                    }

                });
        }

        return propertiesOverriddenMapping;
    }

    PropertiesOverriddenMapping mapPropertiesOverriddenMappingfromDto(List<PropertiesOverriddenMappingDto> mappingEntries) {
        var propertiesOverriddenMapping = PropertiesOverriddenMapping.builder().build();

        for (PropertiesOverriddenMappingDto mappingEntry : elements(mappingEntries).asList()) {
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

    public ClassMetaModel toModelFromEntity(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        if (classMetaModelEntity.getClassName() != null) {
            return metaModelContext.getClassMetaModels().findById(classMetaModelEntity.getId());
        }
        return metaModelContext.findClassMetaModelByName(classMetaModelEntity.getName());
    }

    public ClassMetaModel toModelFromDto(MetaModelContext metaModelContext, ClassMetaModelDto classMetaModelDto) {
        if (classMetaModelDto.getClassName() != null) {
            return classMetaModelMapper.toModelFromDto(classMetaModelDto);
        }
        return metaModelContext.findClassMetaModelByName(classMetaModelDto.getName());
    }

    private boolean resolveNullableBoolean(Boolean nullableBoolean) {
        return Optional.ofNullable(nullableBoolean)
            .orElse(false);
    }
}

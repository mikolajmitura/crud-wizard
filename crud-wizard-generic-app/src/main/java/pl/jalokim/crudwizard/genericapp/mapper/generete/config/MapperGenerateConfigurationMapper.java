package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverStrategyType.READ;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverStrategyType.WRITE;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.replaceAllWithEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverStrategyType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ReadFieldResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.WriteFieldResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.ReadFieldMetaResolverForClassEntryDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.WriteFieldMetaResolverForClassEntryDto;
import pl.jalokim.utils.collection.Elements;

@Mapper(config = MapperAsSpringBeanConfig.class, uses = AdditionalPropertyMapper.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public abstract class MapperGenerateConfigurationMapper {

    @Autowired
    private ClassMetaModelMapper classMetaModelMapper;

    public abstract MapperGenerateConfigurationEntity mapToEntity(MapperGenerateConfigurationDto mapperGenerateConfigurationDto);

    @Mapping(target = "fieldMetaResolverForClass", source = "fieldMetaResolverConfigurationDto")
    public abstract FieldMetaResolverConfigurationEntity mapFieldResolverConfEntity(FieldMetaResolverConfigurationDto fieldMetaResolverConfigurationDto);

    protected List<FieldMetaResolverForClassEntryEntity> mergeConfigs(FieldMetaResolverConfigurationDto fieldMetaResolverConfigurationDto) {
        List<FieldMetaResolverForClassEntryEntity> entries = new ArrayList<>();
        elements(fieldMetaResolverConfigurationDto.getReadFieldMetaResolverForClass())
            .map(entry -> mapToEntity(entry, READ))
            .asList();
        elements(fieldMetaResolverConfigurationDto.getWriteFieldMetaResolverForClass())
            .map(entry -> mapToEntity(entry, WRITE))
            .asList();
        return entries;
    }

    @Mapping(target = "id", ignore = true)
    protected abstract FieldMetaResolverForClassEntryEntity mapToEntity(FieldMetaResolverForClassEntryDto configurationDto,
        FieldMetaResolverStrategyType fieldMetaResolverStrategyType);

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


    @Mapping(target = "readFieldMetaResolverForClass", source = "fieldMetaResolverConfiguration")
    @Mapping(target = "writeFieldMetaResolverForClass", source = "fieldMetaResolverConfiguration")
    public abstract FieldMetaResolverConfiguration mapFieldMetaResolverConfiguration(FieldMetaResolverConfigurationEntity fieldMetaResolverConfiguration);

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

    Map<Class<?>, ReadFieldResolver> mapReadFieldResolverForClassFromEntity(FieldMetaResolverConfigurationEntity fieldMetaResolverConfiguration) {
        return elements(fieldMetaResolverConfiguration.getFieldMetaResolverForClass())
            .filter(fieldMetaResolversForEntry -> fieldMetaResolversForEntry.getFieldMetaResolverStrategyType().equals(READ))
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createReadMetaResolver(entry.getResolverClassName()));
    }

    Map<Class<?>, WriteFieldResolver> mapWriteFieldResolverForClassFromEntity(FieldMetaResolverConfigurationEntity fieldMetaResolverConfiguration) {
        return elements(fieldMetaResolverConfiguration.getFieldMetaResolverForClass())
            .filter(fieldMetaResolversForEntry -> fieldMetaResolversForEntry.getFieldMetaResolverStrategyType().equals(WRITE))
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createWriteMetaResolver(entry.getResolverClassName()));
    }

    @Mapping(target = "readFieldMetaResolverForClass", source = "fieldMetaResolverConfigurationDto")
    @Mapping(target = "writeFieldMetaResolverForClass", source = "fieldMetaResolverConfigurationDto")
    protected abstract FieldMetaResolverConfiguration mapMapperConfigurationFromDto(FieldMetaResolverConfigurationDto fieldMetaResolverConfigurationDto);

    Map<Class<?>, ReadFieldResolver> mapReadFieldResolverForClassFromDto(FieldMetaResolverConfigurationDto fieldMetaResolverConfiguration) {
        return elements(fieldMetaResolverConfiguration.getReadFieldMetaResolverForClass())
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createReadMetaResolver(entry.getResolverClassName()));
    }

    Map<Class<?>, WriteFieldResolver> mapWriteFieldResolverForClassFromDto(FieldMetaResolverConfigurationDto fieldMetaResolverConfiguration) {
        return elements(fieldMetaResolverConfiguration.getWriteFieldMetaResolverForClass())
            .asMap(entry -> ClassUtils.loadRealClass(entry.getClassName()),
                entry -> FieldMetaResolverFactory.createWriteMetaResolver(entry.getResolverClassName()));
    }

    @Mapping(target = "writeFieldMetaResolverForClass", source = "fieldMetaResolverConfigurationEntity")
    @Mapping(target = "readFieldMetaResolverForClass", source = "fieldMetaResolverConfigurationEntity")
    public abstract FieldMetaResolverConfigurationDto mapFieldResolverConfDto(FieldMetaResolverConfigurationEntity fieldMetaResolverConfigurationEntity);

    protected List<WriteFieldMetaResolverForClassEntryDto> mapWriteFieldMetaResolverForClass(FieldMetaResolverConfigurationEntity entity) {
        return elements(entity.getFieldMetaResolverForClass())
            .filter(entry -> entry.getFieldMetaResolverStrategyType().equals(WRITE))
            .map(this::mapWriteFieldMetaResolverForClassEntryDto)
            .asList();
    };

    protected List<ReadFieldMetaResolverForClassEntryDto> mapReadFieldMetaResolverForClass(FieldMetaResolverConfigurationEntity entity) {
        return elements(entity.getFieldMetaResolverForClass())
            .filter(entry -> entry.getFieldMetaResolverStrategyType().equals(READ))
            .map(this::mapReadFieldMetaResolverForClassEntryDto)
            .asList();
    };

    protected abstract WriteFieldMetaResolverForClassEntryDto mapWriteFieldMetaResolverForClassEntryDto(FieldMetaResolverForClassEntryEntity entity);

    protected abstract ReadFieldMetaResolverForClassEntryDto mapReadFieldMetaResolverForClassEntryDto(FieldMetaResolverForClassEntryEntity entity);

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

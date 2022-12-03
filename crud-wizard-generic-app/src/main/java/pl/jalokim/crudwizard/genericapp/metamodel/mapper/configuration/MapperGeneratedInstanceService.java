package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import static pl.jalokim.crudwizard.core.datetime.TimeProviderHolder.getTimeProvider;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator.GENERATED_MAPPER_PACKAGE;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.compiler.ClassLoaderService;
import pl.jalokim.crudwizard.genericapp.compiler.CodeCompiler;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MappingEntryModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMappingResolver;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.MapperConfigurationParserContext;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.util.GeneratedCodeMd5Generator;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;

@MetamodelService
@RequiredArgsConstructor
@Slf4j
public class MapperGeneratedInstanceService {

    private final MapperCodeGenerator mapperCodeGenerator;
    private final MapperGenerateConfigurationMapper mapperGenerateConfigurationMapper;
    private final CodeCompiler codeCompiler;
    private final ClassLoaderService classLoaderService;
    private final MapperCompiledCodeMetadataRepository mapperCompiledCodeMetadataRepository;
    private final ApplicationContext applicationContext;
    private final PropertiesOverriddenMappingResolver propertiesOverriddenMappingResolver;
    private final InstanceLoader instanceLoader;

    public Object loadMapperInstanceOrGenerateNew(MapperGenerateConfigurationEntity mapperGenerateConfigurationEntity,
        MetaModelContext metaModelContext) {
        try {
            Long currentTimestamp = getTimeProvider().getCurrentTimestamp();
            ClassMetaModel pathVariablesClassModel = findClassMetaModelFor(metaModelContext, mapperGenerateConfigurationEntity.getPathVariablesClassModel());
            ClassMetaModel requestParamsClassModel = findClassMetaModelFor(metaModelContext, mapperGenerateConfigurationEntity.getRequestParamsClassModel());

            var mapperGenerateConfiguration = mapperGenerateConfigurationMapper.mapConfiguration(mapperGenerateConfigurationEntity,
                pathVariablesClassModel, requestParamsClassModel, metaModelContext);

            parseOverriddenPropertiesForMapper(mapperGenerateConfiguration, mapperGenerateConfiguration.getRootConfiguration(),
                mapperGenerateConfigurationEntity.getRootConfiguration());

            elements(mapperGenerateConfigurationEntity.getSubMappersAsMethods())
                .forEach(subMapperConfig -> parseOverriddenPropertiesForMapper(mapperGenerateConfiguration,
                    mapperGenerateConfiguration.getMapperConfigurationByMethodName(subMapperConfig.getName()),
                    subMapperConfig));

            var mapperCodeMetadata = mapperCodeGenerator.generateMapperCodeMetadata(mapperGenerateConfiguration, currentTimestamp);
            var generatedMapperCode = mapperCodeGenerator.generateMapperCode(mapperCodeMetadata);

            String newCodeMd5 = GeneratedCodeMd5Generator
                .generateMd5Hash(mapperCodeMetadata.getMapperClassName(), generatedMapperCode, currentTimestamp);

            var mapperCompiledCodeMetadata = mapperGenerateConfigurationEntity.getMapperCompiledCodeMetadata();
            if (newCodeMd5.equals(mapperCompiledCodeMetadata.getGeneratedCodeHash())) {
                log.info("use earlier compiled class: {}", mapperCompiledCodeMetadata.getFullClassName());
                return classLoaderService.loadClass(mapperCompiledCodeMetadata.getFullClassName(), mapperCompiledCodeMetadata.getSessionGenerationTimestamp());
            } else {
                var newCompiledCodeMetadata = codeCompiler.compileCodeAndReturnMetaData(mapperCodeMetadata.getMapperClassName(),
                    GENERATED_MAPPER_PACKAGE, generatedMapperCode, currentTimestamp);
                String newClassLoaderName = currentTimestamp.toString();
                classLoaderService.createNewClassLoader(newClassLoaderName);
                log.info("compiled code for: {} for mapper: {}", newCompiledCodeMetadata.getFullClassName(),
                    mapperGenerateConfigurationEntity.getRootConfiguration().getName());

                mapperCompiledCodeMetadata.setFullPath(newCompiledCodeMetadata.getFullPath());
                mapperCompiledCodeMetadata.setFullClassName(newCompiledCodeMetadata.getFullClassName());
                mapperCompiledCodeMetadata.setSimpleClassName(newCompiledCodeMetadata.getSimpleClassName());
                mapperCompiledCodeMetadata.setSessionGenerationTimestamp(newCompiledCodeMetadata.getSessionGenerationTimestamp());
                mapperCompiledCodeMetadata.setGeneratedCodeHash(newCodeMd5);

                mapperCompiledCodeMetadataRepository.save(mapperCompiledCodeMetadata);

                Class<?> newMapperInstance = classLoaderService.loadClass(newCompiledCodeMetadata.getFullClassName(), newClassLoaderName);
                return instanceLoader.createInstanceOrGetBean(newMapperInstance);
            }
        } catch (Exception exception) {
            // TODO #1 how to rollback to earlier version of application when exception occurred?
            //  for example somebody updated class metamodel and few mappers can inform about exception... how to fix the problems then?
            //  new version of class metamodels does not exists and then mappers cannot be fixed which are created in others endpoints
            throw new IllegalStateException(exception);
        }
    }

    private ClassMetaModel findClassMetaModelFor(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        return Optional.ofNullable(classMetaModelEntity)
            .map(ClassMetaModelEntity::getId)
            .map(id -> metaModelContext.getClassMetaModels().findById(id))
            .orElse(null);
    }

    private void parseOverriddenPropertiesForMapper(MapperGenerateConfiguration mapperGenerateConfiguration,
        MapperConfiguration mapperConfiguration, MapperConfigurationEntity mapperConfigurationEntity) {

        elements(mapperConfigurationEntity.getPropertyOverriddenMapping())
            .forEach(propertyOverriddenMapping -> {
                MapperConfigurationParserContext parserContext = new MapperConfigurationParserContext(applicationContext, mapperGenerateConfiguration);
                var fieldMetaResolverForRawTarget = mapperGenerateConfiguration.getFieldMetaResolverForRawTarget();
                parserContext.setCurrentMapperConfiguration(mapperConfiguration);

                MappingEntryModel mappingEntryModel = new MappingEntryModel(propertyOverriddenMapping.getTargetAssignPath(),
                    propertyOverriddenMapping.getSourceAssignExpression());
                propertiesOverriddenMappingResolver.parseMappingEntry(parserContext, mappingEntryModel, 0, fieldMetaResolverForRawTarget);
            });
    }
}

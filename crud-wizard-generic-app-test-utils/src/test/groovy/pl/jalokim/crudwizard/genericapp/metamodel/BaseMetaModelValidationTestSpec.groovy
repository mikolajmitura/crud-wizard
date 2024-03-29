package pl.jalokim.crudwizard.genericapp.metamodel

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField

import javax.validation.ValidatorFactory
import org.mapstruct.factory.Mappers
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.core.convert.ConversionService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import pl.jalokim.crudwizard.genericapp.compiler.CodeCompiler
import pl.jalokim.crudwizard.genericapp.compiler.CompiledCodeRootPathProvider
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorageFactory
import pl.jalokim.crudwizard.genericapp.mapper.conversion.ClassMetaModelConverter
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapperImpl
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMappingResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.AssignExpressionAsTextResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.EnumsMapperMethodGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.SimpleTargetAssignResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.ClassMetaModelMapperParser
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapperImpl
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.CommonClassAndFieldMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.CommonClassAndFieldMapperImpl
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelMapperImpl
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.TemporaryContextLoader
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelTypeExtractor
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageInstances
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.BeforeEndpointValidatorUpdater
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelMapperImpl
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeDtoWithFields
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper
import pl.jalokim.crudwizard.genericapp.method.BeanMethodMetaModelCreator
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

class BaseMetaModelValidationTestSpec extends UnitTestSpec {

    ClassMetaModelRepository classMetaModelRepository = Mock()
    MetaModelContextService metaModelContextService = Mock()
    JdbcTemplate jdbcTemplate = Mock()
    ValidatorFactory validatorFactory = Mock()
    DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository = Mock()
    DataStorageInstances dataStorageInstances = Mock()
    AdditionalPropertyMapper additionalPropertyMapper = Mappers.getMapper(AdditionalPropertyMapper)
    TranslationMapper translationMapper = Mappers.getMapper(TranslationMapper)
    CommonClassAndFieldMapper commonClassAndFieldMapper = new CommonClassAndFieldMapperImpl(additionalPropertyMapper, translationMapper)
    ClassMetaModelMapper classMetaModelMapper = new ClassMetaModelMapperImpl(additionalPropertyMapper, translationMapper, commonClassAndFieldMapper)
    MapperGenerateConfigurationMapper mapperGenerateConfigurationMapper = new MapperGenerateConfigurationMapperImpl(classMetaModelMapper)
    MapperMetaModelMapper mapperMetaModelMapper = new MapperMetaModelMapperImpl(additionalPropertyMapper, mapperGenerateConfigurationMapper,
        classMetaModelMapper)
    ClassMetaModelTypeExtractor classMetaModelTypeExtractor = new ClassMetaModelTypeExtractor(classMetaModelMapper)
    JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(createObjectMapper())
    EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils = new EndpointMetaModelContextNodeUtils(jsonObjectMapper, metaModelContextService)
    MethodSignatureMetaModelResolver methodSignatureMetaModelResolver = new MethodSignatureMetaModelResolver(jsonObjectMapper)
    BeforeEndpointValidatorUpdater beforeEndpointValidatorUpdater = new BeforeEndpointValidatorUpdater()
    TemporaryContextLoader temporaryContextLoader = new TemporaryContextLoader(validatorFactory,
        metaModelContextService, classMetaModelMapper, mapperMetaModelMapper
    )

    PropertiesOverriddenMappingResolver propertiesOverriddenMappingResolver = new PropertiesOverriddenMappingResolver(applicationContext)

    ClassMetaModelMapperParser classMetaModelMapperParser = new ClassMetaModelMapperParser(metaModelContextService)

    RequestMappingHandlerMapping abstractHandlerMethodMapping = Mock()
    ConversionService conversionService = Mock()
    GenericObjectsConversionService genericObjectsConversionService = new GenericObjectsConversionService(applicationContext)
    AssignExpressionAsTextResolver assignExpressionAsTextResolver = new AssignExpressionAsTextResolver(genericObjectsConversionService, conversionService)
    EnumsMapperMethodGenerator enumsMapperMethodGenerator = new EnumsMapperMethodGenerator()
    SimpleTargetAssignResolver simpleTargetAssignResolver = new SimpleTargetAssignResolver(genericObjectsConversionService,
        assignExpressionAsTextResolver, enumsMapperMethodGenerator)
    MapperMethodGenerator mapperMethodGenerator = new MapperMethodGenerator(genericObjectsConversionService, assignExpressionAsTextResolver,
        simpleTargetAssignResolver, conversionService, instanceLoader
    )
    MapperCodeGenerator mapperCodeGenerator = new MapperCodeGenerator(mapperMethodGenerator, enumsMapperMethodGenerator)
    CompiledCodeRootPathProvider codeRootPathProvider = new CompiledCodeRootPathProvider("target/generatedMappers")
    CodeCompiler codeCompiler = new CodeCompiler(codeRootPathProvider)

    ValidatorWithConverter validatorWithConverter

    def setup() {
        applicationContextMapping.put(ClassMetaModelMapperParser, classMetaModelMapperParser)
        applicationContextMapping.put(MapperMethodGenerator, mapperMethodGenerator)
        applicationContextMapping.put(MetaModelContextService, metaModelContextService)

        abstractHandlerMethodMapping.getHandlerMethods() >> [:]
        dataStorageInstances.getDataStorageFactoryForClass(_) >> Mock(DataStorageFactory)

        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> 0
        def fieldMetaModelMapper = new FieldMetaModelMapperImpl(additionalPropertyMapper, translationMapper, commonClassAndFieldMapper)
        FieldMetaModelService fieldMetaModelService = new FieldMetaModelService(fieldMetaModelMapper)
        setValueForField(classMetaModelMapper, "fieldMetaModelMapper", fieldMetaModelMapper)
        setValueForField(classMetaModelMapper, "commonClassAndFieldMapper", commonClassAndFieldMapper)
        setValueForField(classMetaModelMapper, ClassMetaModelMapper, "commonClassAndFieldMapper", commonClassAndFieldMapper)
        setValueForField(fieldMetaModelMapper, "commonClassAndFieldMapperInjected", commonClassAndFieldMapper)

        setValueForField(mapperMetaModelMapper, "instanceLoader", instanceLoader)
        setValueForField(mapperMetaModelMapper, "beanMethodMetaModelCreator", new BeanMethodMetaModelCreator(
            new MethodSignatureMetaModelResolver(jsonObjectMapper)))
        setValueForField(mapperMetaModelMapper, MapperMetaModelMapper.class, "classMetaModelMapper", classMetaModelMapper)
        setValueForField(mapperMetaModelMapper, "methodSignatureMetaModelResolver", methodSignatureMetaModelResolver)

        setValueForField(mapperGenerateConfigurationMapper, MapperGenerateConfigurationMapper, "classMetaModelMapper", classMetaModelMapper)

        applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class) >> abstractHandlerMethodMapping
        applicationContext.getBean("someBean") >> {
            throw new NoSuchBeanDefinitionException("someBean")
        }
        applicationContext.getBeanNamesForType(ClassMetaModelConverter.class) >> []
        conversionService.canConvert(Long, String) >> true
        conversionService.canConvert(Integer, String) >> true

        validatorWithConverter = createValidatorWithConverter(endpointMetaModelContextNodeUtils, applicationContext,
            dataStorageConnectorMetaModelRepository, classMetaModelTypeExtractor, metaModelContextService,
            jdbcTemplate, dataStorageInstances, methodSignatureMetaModelResolver, classMetaModelMapper,
            mapperGenerateConfigurationMapper, propertiesOverriddenMappingResolver, mapperCodeGenerator,
            codeCompiler, fieldMetaModelService, classMetaModelRepository)

        validatorFactory.getValidator() >> validatorWithConverter.getValidator()
        classMetaModelRepository.findByClassName(SomeDtoWithFields.canonicalName) >> [
            ClassMetaModelEntity.builder()
                .className(SomeDtoWithFields.canonicalName)
                .fields([
                    FieldMetaModelEntity.builder().build()
                ])
                .build()
        ]
        classMetaModelRepository.findByClassName(_ as String) >> []
    }

    def cleanup() {
        TemporaryModelContextHolder.clearTemporaryMetaModelContext()
    }
}

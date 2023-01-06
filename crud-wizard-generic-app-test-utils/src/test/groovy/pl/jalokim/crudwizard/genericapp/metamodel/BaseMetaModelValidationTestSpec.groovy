package pl.jalokim.crudwizard.genericapp.metamodel

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField

import javax.validation.ValidatorFactory
import org.mapstruct.factory.Mappers
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
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
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMappingResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.AssignExpressionAsTextResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.EnumsMapperMethodGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.SimpleTargetAssignResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.CastMetaModelSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.ClassMetaModelMapperParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.FieldChainOrEachMapByExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.InitSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.InnerMethodSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.OtherVariableSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.RawJavaCodeSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.SpringBeanOrOtherMapperParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WriteToMapStrategy
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.RawAdditionalPropertyMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.TemporaryContextLoader
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelTypeExtractor
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageInstances
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.BeforeEndpointValidatorUpdater
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelMapper
import pl.jalokim.crudwizard.genericapp.method.BeanMethodMetaModelCreator
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

class BaseMetaModelValidationTestSpec extends UnitTestSpec {

    MetaModelContextService metaModelContextService = Mock()
    ApplicationContext applicationContext = Mock()
    JdbcTemplate jdbcTemplate = Mock()
    ValidatorFactory validatorFactory = Mock()
    DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository = Mock()
    DataStorageInstances dataStorageInstances = Mock()
    ClassMetaModelMapper classMetaModelMapper = Mappers.getMapper(ClassMetaModelMapper)
    MapperMetaModelMapper mapperMetaModelMapper = Mappers.getMapper(MapperMetaModelMapper)
    ClassMetaModelTypeExtractor classMetaModelTypeExtractor = new ClassMetaModelTypeExtractor(classMetaModelMapper)
    JsonObjectMapper jsonObjectMapper = new JsonObjectMapper(createObjectMapper())
    EndpointMetaModelContextNodeUtils endpointMetaModelContextNodeUtils = new EndpointMetaModelContextNodeUtils(jsonObjectMapper, metaModelContextService)
    MethodSignatureMetaModelResolver methodSignatureMetaModelResolver = new MethodSignatureMetaModelResolver(jsonObjectMapper)
    BeforeEndpointValidatorUpdater beforeEndpointValidatorUpdater = new BeforeEndpointValidatorUpdater()
    TemporaryContextLoader temporaryContextLoader = new TemporaryContextLoader(validatorFactory,
        metaModelContextService, classMetaModelMapper, mapperMetaModelMapper
    )
    MapperGenerateConfigurationMapper mapperGenerateConfigurationMapper = Mappers.getMapper(MapperGenerateConfigurationMapper)
    PropertiesOverriddenMappingResolver propertiesOverriddenMappingResolver = new PropertiesOverriddenMappingResolver(applicationContext)

    ClassMetaModelMapperParser classMetaModelMapperParser = new ClassMetaModelMapperParser(metaModelContextService)

    GenericBeansProvider genericBeanProvider = Mock()
    RequestMappingHandlerMapping abstractHandlerMethodMapping = Mock()
    ConversionService conversionService = Mock()
    GenericObjectsConversionService genericObjectsConversionService = new GenericObjectsConversionService(applicationContext)
    InstanceLoader instanceLoader = new InstanceLoader(applicationContext)
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

    ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(endpointMetaModelContextNodeUtils, applicationContext,
        dataStorageConnectorMetaModelRepository, classMetaModelTypeExtractor, metaModelContextService,
        jdbcTemplate, dataStorageInstances, methodSignatureMetaModelResolver, classMetaModelMapper,
        mapperGenerateConfigurationMapper, propertiesOverriddenMappingResolver, mapperCodeGenerator, codeCompiler)

    Map<Class, Object> applicationContextMapping = [:]

    def setup() {
        applicationContextMapping.put(InitSourceExpressionParser, new InitSourceExpressionParser(applicationContext))
        applicationContextMapping.put(OtherVariableSourceExpressionParser, new OtherVariableSourceExpressionParser(applicationContext))
        applicationContextMapping.put(FieldChainOrEachMapByExpressionParser, new FieldChainOrEachMapByExpressionParser(applicationContext))
        applicationContextMapping.put(SpringBeanOrOtherMapperParser, new SpringBeanOrOtherMapperParser(applicationContext, metaModelContextService))
        applicationContextMapping.put(RawJavaCodeSourceExpressionParser, new RawJavaCodeSourceExpressionParser(applicationContext))
        applicationContextMapping.put(InnerMethodSourceExpressionParser, new InnerMethodSourceExpressionParser(applicationContext))
        applicationContextMapping.put(CastMetaModelSourceExpressionParser,
            new CastMetaModelSourceExpressionParser(applicationContext, classMetaModelMapperParser))
        applicationContextMapping.put(WriteToMapStrategy, new WriteToMapStrategy())
        applicationContextMapping.put(MapperMethodGenerator, mapperMethodGenerator)

        validatorFactory.getValidator() >> validatorWithConverter.getValidator()
        abstractHandlerMethodMapping.getHandlerMethods() >> [:]
        dataStorageInstances.getDataStorageFactoryForClass(_) >> Mock(DataStorageFactory)

        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> 0
        setValueForField(classMetaModelMapper, "fieldMetaModelMapper", Mappers.getMapper(FieldMetaModelMapper))
        setValueForField(classMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))

        setValueForField(mapperMetaModelMapper, "instanceLoader", instanceLoader)
        setValueForField(mapperMetaModelMapper, "beanMethodMetaModelCreator", new BeanMethodMetaModelCreator(
            new MethodSignatureMetaModelResolver(jsonObjectMapper)))
        setValueForField(mapperMetaModelMapper, "classMetaModelMapper", classMetaModelMapper)
        setValueForField(mapperMetaModelMapper, "methodSignatureMetaModelResolver", methodSignatureMetaModelResolver)

        setValueForField(mapperGenerateConfigurationMapper, "classMetaModelMapper", classMetaModelMapper)

        applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class) >> abstractHandlerMethodMapping
        applicationContext.getBean("someBean") >> {
            throw new NoSuchBeanDefinitionException("someBean")
        }

        applicationContext.getBean(_ as Class) >> {args ->
            Class type = args[0]
            def bean = applicationContextMapping.get(type)
            if (bean) {
                return bean
            }
            throw new IllegalArgumentException("Cannot find bean with type: ${type.canonicalName}")
        }
        applicationContext.getBeanNamesForType(ClassMetaModelConverter.class) >> []
        conversionService.canConvert(Long, String) >> true
        conversionService.canConvert(Integer, String) >> true
    }

    def cleanup() {
        TemporaryModelContextHolder.clearTemporaryMetaModelContext()
    }
}

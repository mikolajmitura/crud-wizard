package pl.jalokim.crudwizard.genericapp.metamodel

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField

import javax.validation.ValidatorFactory
import org.mapstruct.factory.Mappers
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorageFactory
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMappingResolver
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.CastMetaModelSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.ClassMetaModelMapperParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.FieldChainOrEachMapByExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.InitSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.InnerMethodSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.OtherVariableSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.RawJavaCodeSourceExpressionParser
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.SpringBeanOrOtherMapperParser
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
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider
import pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator
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

    ValidatorWithConverter validatorWithConverter = createValidatorWithConverter(endpointMetaModelContextNodeUtils, applicationContext,
        dataStorageConnectorMetaModelRepository, classMetaModelTypeExtractor, metaModelContextService,
        jdbcTemplate, dataStorageInstances, methodSignatureMetaModelResolver, classMetaModelMapper,
        mapperGenerateConfigurationMapper, propertiesOverriddenMappingResolver)

    RequestMappingHandlerMapping abstractHandlerMethodMapping = Mock()

    Map<Class, Object> applicationContextMapping = Map.of(
        InitSourceExpressionParser, new InitSourceExpressionParser(applicationContext),
        OtherVariableSourceExpressionParser, new OtherVariableSourceExpressionParser(applicationContext),
        FieldChainOrEachMapByExpressionParser, new FieldChainOrEachMapByExpressionParser(applicationContext),
        SpringBeanOrOtherMapperParser, new SpringBeanOrOtherMapperParser(applicationContext, metaModelContextService),
        RawJavaCodeSourceExpressionParser, new RawJavaCodeSourceExpressionParser(applicationContext),
        InnerMethodSourceExpressionParser, new InnerMethodSourceExpressionParser(applicationContext),
        CastMetaModelSourceExpressionParser, new CastMetaModelSourceExpressionParser(applicationContext, classMetaModelMapperParser),
    )

    def setup() {
        validatorFactory.getValidator() >> validatorWithConverter.getValidator()
        abstractHandlerMethodMapping.getHandlerMethods() >> [:]
        dataStorageInstances.getDataStorageFactoryForClass(_) >> Mock(DataStorageFactory)

        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> 0
        setValueForField(classMetaModelMapper, "fieldMetaModelMapper", Mappers.getMapper(FieldMetaModelMapper))
        setValueForField(classMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))

        GenericBeansProvider genericBeanProvider = Mock()
        InstanceLoader instanceLoader = Mock()

        setValueForField(mapperMetaModelMapper, "genericBeanProvider", genericBeanProvider)
        setValueForField(mapperMetaModelMapper, "instanceLoader", instanceLoader)
        setValueForField(mapperMetaModelMapper, "beanMethodMetaModelCreator", new BeanMethodMetaModelCreator(
            new MethodSignatureMetaModelResolver(jsonObjectMapper)))
        setValueForField(mapperMetaModelMapper, "classMetaModelMapper", classMetaModelMapper)

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
    }

    def cleanup() {
        TemporaryModelContextHolder.clearTemporaryMetaModelContext()
    }
}

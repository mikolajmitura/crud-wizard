package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.getRequiredFieldFromClassModel

import org.springframework.context.ApplicationContext
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.test.utils.UnitTestSpec

class BaseSourceExpressionParserTestSpec extends UnitTestSpec {

    ApplicationContext applicationContext = Mock()
    MetaModelContextService metaModelContextService = Mock()
    MetaModelContext metaModelContext = Mock()
    MapperConfiguration mapperConfiguration = Mock()
    MapperGenerateConfiguration mapperGenerateConfiguration = createMapperGenerateConfiguration()
    ClassMetaModelMapperParser classMetaModelMapperParser = new ClassMetaModelMapperParser(metaModelContextService)
    SourceExpressionParserContext sourceExpressionParserContext

    def mapperConfigurationParserContext = new MapperConfigurationParserContext(applicationContext, mapperGenerateConfiguration)
    def initSourceExpressionParser = new InitSourceExpressionParser(applicationContext)
    def springBeanOrOtherMapperParser = new SpringBeanOrOtherMapperParser(applicationContext, metaModelContextService)
    def otherVariableSourceExpressionParser = new OtherVariableSourceExpressionParser(applicationContext)
    def innerMethodSourceExpressionParser = new InnerMethodSourceExpressionParser(applicationContext)
    def castMetaModelSourceExpressionParser = new CastMetaModelSourceExpressionParser(applicationContext, classMetaModelMapperParser)
    def rawJavaCodeSourceExpressionParser = new RawJavaCodeSourceExpressionParser(applicationContext)
    def fieldChainSourceExpressionParser = new FieldChainOrEachMapByExpressionParser(applicationContext)

    protected MapperGenerateConfiguration createMapperGenerateConfiguration() {
        Mock(MapperGenerateConfiguration)
    }

    def setup() {
        applicationContext.getBean(InitSourceExpressionParser) >> initSourceExpressionParser
        applicationContext.getBean(SpringBeanOrOtherMapperParser) >> springBeanOrOtherMapperParser
        applicationContext.getBean(OtherVariableSourceExpressionParser) >> otherVariableSourceExpressionParser
        applicationContext.getBean(InnerMethodSourceExpressionParser) >> innerMethodSourceExpressionParser
        applicationContext.getBean(CastMetaModelSourceExpressionParser) >> castMetaModelSourceExpressionParser
        applicationContext.getBean(RawJavaCodeSourceExpressionParser) >> rawJavaCodeSourceExpressionParser
        applicationContext.getBean(FieldChainOrEachMapByExpressionParser) >> fieldChainSourceExpressionParser

        metaModelContextService.getMetaModelContext() >> metaModelContext
        mapperGenerateConfiguration.getFieldMetaResolverForRawSource() >> READ_FIELD_RESOLVER_CONFIG
    }

    protected <T extends ValueToAssignExpression> T parseExpression(String expression) {
        parseExpression(expression, null)
    }

    protected <T extends ValueToAssignExpression> T parseExpression(String expression, ClassMetaModel targetFieldClassMetaModel) {
        sourceExpressionParserContext = createSourceExpressionParserContext(expression, targetFieldClassMetaModel)

        (T) initSourceExpressionParser.mainParse(mapperConfigurationParserContext, sourceExpressionParserContext)
    }

    protected FieldMetaModel getFieldMetaModelByName(Class<?> rawClass, String fieldName) {
        getRequiredFieldFromClassModel(createClassMetaModelFromClass(rawClass), fieldName, READ_FIELD_RESOLVER_CONFIG)
    }

    protected SourceExpressionParserContext createSourceExpressionParserContext(String expression, ClassMetaModel targetFieldClassMetaModel) {
        def sourceExpressionParserContext = new SourceExpressionParserContext(expression, mapperConfigurationParserContext,
            mapperConfiguration, targetFieldClassMetaModel)
        mapperConfigurationParserContext.setInitColumnNumber(0)
        sourceExpressionParserContext
    }
}

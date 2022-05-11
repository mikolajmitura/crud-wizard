package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.concatElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ByMapperNameAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Component
class SpringBeanOrOtherMapperParser extends SourceExpressionParser {

    private final MetaModelContextService metaModelContextService;

    public SpringBeanOrOtherMapperParser(ApplicationContext applicationContext, MetaModelContextService metaModelContextService) {
        super(applicationContext);
        this.metaModelContextService = metaModelContextService;
    }

    @Override
    public ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        sourceExpressionParserContext.skipSpaces();
        if (sourceExpressionParserContext.isLastCurrentChar()) {
            mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("expected.beanName.or.mapperName"));
        }

        CollectedExpressionPartResult beanOrMapperNamePart = sourceExpressionParserContext.collectTextUntilAnyChars('(', '.');
        String beanOrMapperName = beanOrMapperNamePart.getCollectedText().trim();

        if (beanOrMapperNamePart.getCutWithText() == '.') {
            return createBySpringBeanMethodAssignExpression(mapperConfigurationParserContext,
                sourceExpressionParserContext, beanOrMapperName);

        } else {
            return createByMapperNameAssignExpression(mapperConfigurationParserContext, sourceExpressionParserContext, beanOrMapperName);
        }
    }

    private BySpringBeanMethodAssignExpression createBySpringBeanMethodAssignExpression(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext, String beanOrMapperName) {

        sourceExpressionParserContext.skipSpaces();

        Object foundBean = null;
        try {
            foundBean = getApplicationContext().getBean(beanOrMapperName);
        } catch (BeansException ex) {
            mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("cannot.find.bean.name", beanOrMapperName));
        }

        CollectedExpressionPartResult methodNamePartResult = sourceExpressionParserContext.collectTextUntilAnyChars('(');
        sourceExpressionParserContext.skipSpaces();
        String methodName = methodNamePartResult.getCollectedText().trim();
        List<ValueToAssignExpression> methodArguments = new ArrayList<>();
        List<Class<?>> methodArgumentsClasses = new ArrayList<>();

        char currentChar = sourceExpressionParserContext.getCurrentChar();

        if (currentChar != ')') {
            while (true) {
                ValueToAssignExpression methodArgument = parseWithOtherParser(InitSourceExpressionParser.class,
                    mapperConfigurationParserContext, sourceExpressionParserContext);
                methodArguments.add(methodArgument);

                ClassMetaModel returnClassModel = methodArgument.generateCodeMetadata(new MapperCodeMetadata()).getReturnClassModel();

                if (returnClassModel.hasRealClass()) {
                    methodArgumentsClasses.add(returnClassModel.getRealClass());
                } else {
                    methodArgumentsClasses.add(Map.class);
                }

                sourceExpressionParserContext.skipSpaces();
                currentChar = sourceExpressionParserContext.getCurrentChar();
                if (currentChar == ',') {
                    sourceExpressionParserContext.moveToNextChar();
                    continue;
                }
                if (currentChar == ')') {
                    break;
                }
                throw mapperConfigurationParserContext.createParseException(
                    createMessagePlaceholder("expected.any.of.chars", elements(",", ")")
                        .map(text -> "'" + text + "'")
                        .asConcatText(" "))
                        .translateMessage());
            }
        }

        Class<?> sourceRawBeanClass = ClassUtils.loadRealClass(foundBean.getClass().getCanonicalName());
        try {
            sourceExpressionParserContext.moveToNextCharIfExists();
            MetadataReflectionUtils.getMethod(foundBean, methodName, methodArgumentsClasses);
            return new BySpringBeanMethodAssignExpression(sourceRawBeanClass, beanOrMapperName, methodName, methodArguments);
        } catch (Exception ex) {
            throw mapperConfigurationParserContext.createParseException(createMessagePlaceholder("cannot.find.method.with.arguments",
                Map.of(
                    "methodName", methodName,
                    "classesTypes", concatElements(methodArgumentsClasses, Class::getCanonicalName, ", "),
                    "givenClass", sourceRawBeanClass.getCanonicalName()
                )));
        }
    }

    private ByMapperNameAssignExpression createByMapperNameAssignExpression(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext, String beanOrMapperName) {
        sourceExpressionParserContext.skipSpaces();
        MetaModelContext metaModelContext = metaModelContextService.getMetaModelContext();
        MapperMetaModel mapperMetaModelByName = metaModelContext.getMapperMetaModels().getMapperMetaModelByName(beanOrMapperName);
        ValueToAssignExpression valueExpression;
        try {
            valueExpression = parseWithOtherParser(InitSourceExpressionParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        } catch (EntryMappingParseException mapperConfigurationParseException) {
            mapperConfigurationParserContext.addException(mapperConfigurationParseException);
            mapperConfigurationParserContext.throwParseException("expected.mapper.argument", beanOrMapperName);
            return null;
        }

        ClassMetaModel targetClassMetaModel = mapperMetaModelByName.getTargetClassMetaModel();


        ClassMetaModel sourceClassMetaModelOfMapper = mapperMetaModelByName.getSourceClassMetaModel();
        ClassMetaModel returnClassModelOfExpression = valueExpression.generateCodeMetadata(new MapperCodeMetadata())
            .getReturnClassModel();
        boolean methodArgumentIsOk = false;
        if (sourceClassMetaModelOfMapper.equals(returnClassModelOfExpression)) {
            methodArgumentIsOk = true;
        } else if (sourceClassMetaModelOfMapper.hasRealClass() && returnClassModelOfExpression.hasRealClass()) {
            Class<?> sourceClassOfMapperArgument = sourceClassMetaModelOfMapper.getRealClass();
            Class<?> returnClassOfExpression = returnClassModelOfExpression.getRealClass();
            if (MetadataReflectionUtils.isTypeOf(returnClassOfExpression, sourceClassOfMapperArgument)) {
                methodArgumentIsOk = true;
            }
        }

        if (!methodArgumentIsOk) {
            mapperConfigurationParserContext.throwParseException("expected.mapper.type.argument",
                sourceClassMetaModelOfMapper.getTypeDescription(),
                returnClassModelOfExpression.getTypeDescription(),
                beanOrMapperName);
        }

        sourceExpressionParserContext.skipSpaces();
        sourceExpressionParserContext.currentCharIs(')');
        sourceExpressionParserContext.moveToNextCharIfExists();
        return new ByMapperNameAssignExpression(targetClassMetaModel, valueExpression, beanOrMapperName);
    }
}

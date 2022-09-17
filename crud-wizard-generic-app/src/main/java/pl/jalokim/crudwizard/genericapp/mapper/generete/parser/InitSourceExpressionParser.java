package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.getRequiredFieldFromClassModel;

import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component
class InitSourceExpressionParser extends SourceExpressionParser {

    public InitSourceExpressionParser(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        sourceExpressionParserContext.skipSpaces();
        if (sourceExpressionParserContext.isLastCurrentChar()) {
            return new RawJavaCodeAssignExpression(sourceExpressionParserContext.getSourceMetaModel(), "sourceObject");
        }
        char currentChar = sourceExpressionParserContext.getCurrentChar();
        sourceExpressionParserContext.moveToNextCharIfExists();

        if (currentChar == '@') {
            return parseWithOtherParser(SpringBeanOrOtherMapperParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        }

        if (currentChar == '$') {
            return parseWithOtherParser(OtherVariableSourceExpressionParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        }

        if (currentChar == '#') {
            return parseWithOtherParser(InnerMethodSourceExpressionParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        }

        if (currentChar == '(') {
            return parseWithOtherParser(CastMetaModelSourceExpressionParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        }

        if (currentChar == 'j') {
            return parseWithOtherParser(RawJavaCodeSourceExpressionParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        }

        sourceExpressionParserContext.moveToPreviousChar();
        var fieldNameCollectResult = sourceExpressionParserContext.collectTextUntilFieldExpressionIsFinished('(');
        ClassMetaModel sourceMetaModel = sourceExpressionParserContext.getSourceMetaModel();
        String nextFieldName = validateVariableAndGet(fieldNameCollectResult.getCollectedText(), mapperConfigurationParserContext);
        moveToPreviousWhenShould(sourceExpressionParserContext);

        return new FieldsChainToAssignExpression(sourceMetaModel, "sourceObject",
            List.of(getRequiredFieldFromClassModel(sourceMetaModel, nextFieldName,
                mapperConfigurationParserContext.getFieldMetaResolverForRawSource())));
    }
}

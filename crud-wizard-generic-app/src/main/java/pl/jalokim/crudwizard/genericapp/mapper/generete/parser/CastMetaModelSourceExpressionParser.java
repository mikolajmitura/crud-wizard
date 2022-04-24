package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Component
class CastMetaModelSourceExpressionParser extends SourceExpressionParser {

    private final ClassMetaModelMapperParser classMetaModelMapperParser;

    public CastMetaModelSourceExpressionParser(ApplicationContext applicationContext, ClassMetaModelMapperParser classMetaModelMapperParser) {
        super(applicationContext);
        this.classMetaModelMapperParser = classMetaModelMapperParser;
    }

    @Override
    ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        sourceExpressionParserContext.skipSpaces();
        sourceExpressionParserContext.currentCharIs('(');
        sourceExpressionParserContext.moveToNextChar();
        sourceExpressionParserContext.skipSpaces();
        CollectedExpressionPartResult collectedExpressionPartResult = sourceExpressionParserContext.collectTextUntilAnyChars(')');
        String castToMetaModel = collectedExpressionPartResult.getCollectedText().trim();

        sourceExpressionParserContext.addClassMetaModelCast(classMetaModelMapperParser
            .parseClassMetaModel(castToMetaModel, mapperConfigurationParserContext));

        ValueToAssignExpression assignExpression = parseWithOtherParser(InitSourceExpressionParser.class,
            mapperConfigurationParserContext, sourceExpressionParserContext);

        sourceExpressionParserContext.clearLastCastMetaModel();
        sourceExpressionParserContext.skipSpaces();
        sourceExpressionParserContext.currentCharIs(')');
        sourceExpressionParserContext.moveToNextCharIfExists();

        return assignExpression;
    }
}

package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression.createFieldsChainToAssignExpression;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.getRequiredFieldFromClassModel;

import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.EachElementMapByMethodAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Component
public class FieldChainOrEachMapByExpressionParser extends SourceExpressionParser {

    public static final String EACH_MAP_BY_METHOD = "eachMapBy";

    public FieldChainOrEachMapByExpressionParser(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        sourceExpressionParserContext.skipSpaces();
        sourceExpressionParserContext.currentCharIs('.');

        ValueToAssignExpression earlierExpression = sourceExpressionParserContext.getEarlierValueToAssignExpression();
        if (earlierExpression == null) {
            mapperConfigurationParserContext.throwParseException(
                createMessagePlaceholder("mapper.parser.expected.expression.for.field.extractor"));
        }

        sourceExpressionParserContext.moveToNextChar();
        CollectedExpressionPartResult collectedExpressionPartResult = sourceExpressionParserContext.collectTextUntilFieldExpressionIsFinished('(');
        String nextVariableName = validateVariableAndGet(collectedExpressionPartResult.getCollectedText(),
            mapperConfigurationParserContext);

        if (EACH_MAP_BY_METHOD.equals(nextVariableName) && collectedExpressionPartResult.getCutWithText() == '(') {
            sourceExpressionParserContext.skipSpaces();
            CollectedExpressionPartResult collectedPartForMethodName = sourceExpressionParserContext.collectTextUntilAnyChars(')');
            String methodName = collectedPartForMethodName.getCollectedText().trim();

            // TODO #1 #1 validation that methodName already exists in mapper methods (does the same from InnerMethodSourceExpressionParser)

            return new EachElementMapByMethodAssignExpression(methodName, earlierExpression);
        }

        ValueToAssignExpression nextChainInvokeExpression;
        if (earlierExpression instanceof FieldsChainToAssignExpression) {
            FieldsChainToAssignExpression earlierFieldChainExpression = (FieldsChainToAssignExpression) earlierExpression;
            nextChainInvokeExpression = earlierFieldChainExpression.createExpressionWithNextField(nextVariableName,
                mapperConfigurationParserContext.getFieldMetaResolverForRawSource());

        } else {
            ClassMetaModel returnClassModel = earlierExpression.generateCodeMetadata(new MapperCodeMetadata())
                .getReturnClassModel();

            nextChainInvokeExpression = createFieldsChainToAssignExpression(earlierExpression,
                List.of(getRequiredFieldFromClassModel(returnClassModel, nextVariableName,
                    mapperConfigurationParserContext.getFieldMetaResolverForRawSource())),
                new MapperCodeMetadata());
        }

        moveToPreviousWhenShould(sourceExpressionParserContext);

        return parseNextChainInvokeWhenExists(mapperConfigurationParserContext,
             sourceExpressionParserContext, nextChainInvokeExpression);
    }
}

package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression.createFieldsChainToAssignExpression;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.getRequiredFieldFromClassModel;

import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Component
public class FieldChainSourceExpressionParser extends SourceExpressionParser {

    public FieldChainSourceExpressionParser(ApplicationContext applicationContext) {
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
        CollectedExpressionPartResult collectedExpressionPartResult = sourceExpressionParserContext.collectTextUntilFieldExpressionIsFinished();
        String nextVariableName = validateVariableAndGet(collectedExpressionPartResult.getCollectedText(),
            mapperConfigurationParserContext);

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

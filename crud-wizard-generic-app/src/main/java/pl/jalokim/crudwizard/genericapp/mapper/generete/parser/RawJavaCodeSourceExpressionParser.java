package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.utils.string.StringUtils;

@Component
public class RawJavaCodeSourceExpressionParser extends SourceExpressionParser {

    public RawJavaCodeSourceExpressionParser(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {
        sourceExpressionParserContext.skipSpaces();
        sourceExpressionParserContext.currentCharIs('(');
        sourceExpressionParserContext.moveToNextChar();
        StringBuilder collectedRawTextBuilder = new StringBuilder();

        int foundLeftParenthesis = 1;
        int foundRightParenthesis = 0;
        do {
            var collected = sourceExpressionParserContext.collectTextUntilAnyChars(')');
            foundRightParenthesis++;
            String rawCode = collected.getCollectedText();
            collectedRawTextBuilder.append(rawCode);
            foundLeftParenthesis = foundLeftParenthesis + StringUtils.countSearchedChar(rawCode, '(');
            collectedRawTextBuilder.append(collected.getCutWithText());
        } while (foundLeftParenthesis != foundRightParenthesis && !sourceExpressionParserContext.isReadAll());

        String rawExpression = collectedRawTextBuilder.toString().trim();
        rawExpression = rawExpression.substring(0, rawExpression.length() - 1);
        foundLeftParenthesis = StringUtils.countSearchedChar(rawExpression, '(');
        foundRightParenthesis = StringUtils.countSearchedChar(rawExpression, ')');

        if (foundLeftParenthesis != foundRightParenthesis) {
            mapperConfigurationParserContext.throwParseException("RawJavaCodeSourceExpressionParser.invalid.expression");
        }

        if (StringUtils.isBlank(rawExpression)) {
            mapperConfigurationParserContext.throwParseException("RawJavaCodeSourceExpressionParser.invalid.expression");
        }

        ClassMetaModel expectedClassMetaModel = sourceExpressionParserContext.getCurrentClassMetaModelAsCast();
        if (expectedClassMetaModel == null) {
            expectedClassMetaModel = sourceExpressionParserContext.getTargetFieldClassMetaModel();
        }
        return new RawJavaCodeAssignExpression(expectedClassMetaModel, rawExpression);
    }
}

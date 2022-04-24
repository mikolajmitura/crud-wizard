package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@RequiredArgsConstructor
@Slf4j
abstract class SourceExpressionParser {

    @Getter
    private final ApplicationContext applicationContext;

    final ValueToAssignExpression mainParse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        ValueToAssignExpression valueToAssignExpression = parse(mapperConfigurationParserContext, sourceExpressionParserContext);

        return parseNextChainInvokeWhenExists(mapperConfigurationParserContext, sourceExpressionParserContext, valueToAssignExpression);
    }

    protected ValueToAssignExpression parseNextChainInvokeWhenExists(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext, ValueToAssignExpression valueToAssignExpression) {
        if (sourceExpressionParserContext.isLastCurrentChar()) {
            return valueToAssignExpression;
        }
        sourceExpressionParserContext.skipSpaces();
        char currentChar = sourceExpressionParserContext.getCurrentChar();
        if (currentChar == '.') {
            sourceExpressionParserContext.setEarlierValueToAssignExpression(valueToAssignExpression);
            return parseWithOtherParser(FieldChainSourceExpressionParser.class,
                mapperConfigurationParserContext, sourceExpressionParserContext);
        }

        return valueToAssignExpression;
    }

    abstract ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext);

    public ValueToAssignExpression parseWithOtherParser(Class<? extends SourceExpressionParser> otherParserClass,
        MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        return applicationContext.getBean(otherParserClass)
            .mainParse(mapperConfigurationParserContext, sourceExpressionParserContext);
    }

    String validateVariableAndGet(String variable, MapperConfigurationParserContext mapperConfigurationParserContext) {
        variable = variable.trim();
        if (StringUtils.isBlank(variable)) {
            mapperConfigurationParserContext.throwParseException("expected.variable.name.not.empty");
        }
        return variable;
    }

    public void moveToPreviousWhenShould(SourceExpressionParserContext sourceExpressionParserContext) {
        if (!sourceExpressionParserContext.isLastCurrentChar()) {
            sourceExpressionParserContext.moveToPreviousChar();
        }
    }
}

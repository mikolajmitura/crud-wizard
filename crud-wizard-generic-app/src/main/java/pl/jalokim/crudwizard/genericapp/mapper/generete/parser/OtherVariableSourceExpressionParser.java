package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelConstants.STRING_MODEL;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModelConstants;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Component
public class OtherVariableSourceExpressionParser extends SourceExpressionParser {

    private static final String HEADERS_VARIABLE_NAME = "headers";
    public static final String MAPPING_CONTEXT_VARIABLE_NAME = "mappingContext";

    public OtherVariableSourceExpressionParser(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        List<String> variableParts = new ArrayList<>();

        var currentPathPart = sourceExpressionParserContext.collectTextUntilFieldExpressionIsFinished('[');
        variableParts.add(currentPathPart.getCollectedText().trim());
        sourceExpressionParserContext.skipSpaces();
        if (currentPathPart.getCutWithText() == '[') {
            sourceExpressionParserContext.skipSpaces();
            sourceExpressionParserContext.currentCharIs('\'');
            sourceExpressionParserContext.moveToNextChar();
            var collectTextInMapKeyStyle = sourceExpressionParserContext.collectTextUntilAnyChars('\'');
            variableParts.add(collectTextInMapKeyStyle.getCollectedText().trim());
            sourceExpressionParserContext.skipSpaces();
            sourceExpressionParserContext.currentCharIs(']');
            sourceExpressionParserContext.moveToNextCharIfExists();
        } else if (currentPathPart.getCutWithText() == '.') {
            var collectTextFieldChainStyle = sourceExpressionParserContext.collectTextUntilFieldExpressionIsFinished();
            moveToPreviousWhenShould(sourceExpressionParserContext);
            variableParts.add(collectTextFieldChainStyle.getCollectedText().trim());
        }

        String mainVariable = validateVariableAndGet(variableParts.get(0), mapperConfigurationParserContext);
        ClassMetaModel mainClassMetaModel = null;

        if (mainVariable.equals(HEADERS_VARIABLE_NAME)) {
            mainClassMetaModel = ClassMetaModelConstants.MAP_STRING_STRING_MODEL;
        } else if (mainVariable.equals("pathVariables")) {
            mainClassMetaModel = mapperConfigurationParserContext.getPathVariablesClassModel();
        } else if (mainVariable.equals("requestParams")) {
            mainClassMetaModel = mapperConfigurationParserContext.getRequestParamsClassModel();
        } else if (mainVariable.equals(MAPPING_CONTEXT_VARIABLE_NAME)) {
            mainClassMetaModel = ClassMetaModelConstants.MAP_STRING_OBJECT_MODEL;
        } else if (mainVariable.equals("rootSourceObject")) {
            mainClassMetaModel = mapperConfigurationParserContext.getMapperGenerateConfiguration()
                .getRootConfiguration().getSourceMetaModel();
        } else if (mainVariable.equals("sourceObject")) {
            mainClassMetaModel = sourceExpressionParserContext.getSourceMetaModel();
        } else {
            mapperConfigurationParserContext.throwParseException("invalid.other.variable.name", mainVariable);
        }

        if (variableParts.size() == 1) {
            return new RawJavaCodeAssignExpression(mainClassMetaModel, mainVariable);
        }

        String firstFieldName = variableParts.get(1);
        if (mainVariable.equals(HEADERS_VARIABLE_NAME) || mainVariable.equals(MAPPING_CONTEXT_VARIABLE_NAME)) {
            ClassMetaModel expectedFieldType = mainVariable.equals(HEADERS_VARIABLE_NAME) ? STRING_MODEL
                : sourceExpressionParserContext.getCurrentClassMetaModelAsCast();

            if (expectedFieldType == null) {
                mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("mappingContext.required.cast"));
            }

            String getFromMapExpression = expressionOfGetFieldValueFromRawMap(mainVariable, firstFieldName, expectedFieldType);
            return new RawJavaCodeAssignExpression(expectedFieldType, getFromMapExpression);
        }

        FieldMetaModel firstFieldByName = mainClassMetaModel.getRequiredFieldByName(firstFieldName);
        return new FieldsChainToAssignExpression(mainClassMetaModel, mainVariable, List.of(firstFieldByName));
    }

    private String expressionOfGetFieldValueFromRawMap(String variableName, String mapKeyName, ClassMetaModel expectedFieldType) {
        return String.format("%s.get(\"%s\")", variableName, mapKeyName);
    }
}

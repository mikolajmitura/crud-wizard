package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperGenerateConstants.ROOT_SOURCE_OBJECT_VAR_NAME;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperGenerateConstants.SOURCE_OBJECT_VAR_NAME;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.STRING_MODEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;

@Component
public class OtherVariableSourceExpressionParser extends SourceExpressionParser {

    private static final String HEADERS_VARIABLE_NAME = "headers";
    public static final String MAPPING_CONTEXT_VARIABLE_NAME = "mappingContext";

    private static final Map<String, ClassMetaModelResolver> RESOLVER_BY_VARIABLE = Map.of(
        HEADERS_VARIABLE_NAME, returnMetaModel(ClassMetaModelConstants.MAP_STRING_STRING_MODEL),
        "pathVariables", (mapperCtx, parserCtx) -> mapperCtx.getPathVariablesClassModel(),
        "requestParams", (mapperCtx, parserCtx) -> mapperCtx.getRequestParamsClassModel(),
        MAPPING_CONTEXT_VARIABLE_NAME, returnMetaModel(ClassMetaModelConstants.MAP_STRING_OBJECT_MODEL),
        ROOT_SOURCE_OBJECT_VAR_NAME, (mapperCtx, parserCtx) ->
            mapperCtx.getMapperGenerateConfiguration().getRootConfiguration().getSourceMetaModel(),
        SOURCE_OBJECT_VAR_NAME, (mapperCtx, parserCtx) -> parserCtx.getSourceMetaModel()
    );

    public OtherVariableSourceExpressionParser(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        List<String> variableParts = getVariableParts(sourceExpressionParserContext);

        String mainVariable = validateVariableAndGet(variableParts.get(0), mapperConfigurationParserContext);
        ClassMetaModelResolver classMetaModelResolver = RESOLVER_BY_VARIABLE.get(mainVariable);

        if (classMetaModelResolver == null) {
            mapperConfigurationParserContext.throwParseException("invalid.other.variable.name", mainVariable);
        }

        ClassMetaModel mainClassMetaModel = classMetaModelResolver.apply(mapperConfigurationParserContext, sourceExpressionParserContext);

        if (variableParts.size() == 1) {
            return new RawJavaCodeAssignExpression(mainClassMetaModel, mainVariable);
        }

        String firstFieldName = variableParts.get(1);
        if (HEADERS_VARIABLE_NAME.equals(mainVariable) || MAPPING_CONTEXT_VARIABLE_NAME.equals(mainVariable)) {
            ClassMetaModel expectedFieldType = HEADERS_VARIABLE_NAME.equals(mainVariable) ? STRING_MODEL
                : sourceExpressionParserContext.getCurrentClassMetaModelAsCast();

            if (expectedFieldType == null) {
                mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("mappingContext.required.cast"));
            }

            String getFromMapExpression = expressionOfGetFieldValueFromRawMap(mainVariable, firstFieldName);
            return new RawJavaCodeAssignExpression(expectedFieldType, getFromMapExpression);
        }

        FieldMetaModel firstFieldByName = mainClassMetaModel.getRequiredFieldByName(firstFieldName);
        return new FieldsChainToAssignExpression(mainClassMetaModel, mainVariable, List.of(firstFieldByName));
    }

    private List<String> getVariableParts(SourceExpressionParserContext sourceExpressionParserContext) {
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
        } else {
            sourceExpressionParserContext.moveToPreviousChar();
        }
        return variableParts;
    }

    private String expressionOfGetFieldValueFromRawMap(String variableName, String mapKeyName) {
        return String.format("%s.get(\"%s\")", variableName, mapKeyName);
    }

    private interface ClassMetaModelResolver extends BiFunction<MapperConfigurationParserContext, SourceExpressionParserContext, ClassMetaModel> {

    }

    @Value
    private static class StaticClassMetaModelResolver implements ClassMetaModelResolver {

        ClassMetaModel classMetaModel;

        @Override
        public ClassMetaModel apply(MapperConfigurationParserContext mapperConfigurationParserContext,
            SourceExpressionParserContext sourceExpressionParserContext) {
            return classMetaModel;
        }
    }

    private static StaticClassMetaModelResolver returnMetaModel(ClassMetaModel classMetaModel) {
        return new StaticClassMetaModelResolver(classMetaModel);
    }
}

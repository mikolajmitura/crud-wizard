package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;

import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@Component
class InnerMethodSourceExpressionParser extends SourceExpressionParser {

    public InnerMethodSourceExpressionParser(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    ValueToAssignExpression parse(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext) {

        sourceExpressionParserContext.skipSpaces();
        CollectedExpressionPartResult collectedExpressionPartResult = sourceExpressionParserContext.collectTextUntilAnyChars('(');
        String innerMethodName = collectedExpressionPartResult.getCollectedText().trim();
        ValueToAssignExpression methodArgumentExpression = parseWithOtherParser(InitSourceExpressionParser.class,
            mapperConfigurationParserContext, sourceExpressionParserContext);
        sourceExpressionParserContext.skipSpaces();
        sourceExpressionParserContext.currentCharIs(')');
        MapperConfiguration mapperConfigurationByMethodName = sourceExpressionParserContext.getMapperGenerateConfiguration()
            .getMapperConfigurationByMethodName(innerMethodName);

        if (mapperConfigurationByMethodName == null) {
            mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("cannot.find.method.with.arguments",
                Map.of("methodName", innerMethodName,
                    "classesTypes", methodArgumentExpression.generateCodeMetadata().getReturnClassModel().getTypeDescription(),
                    "givenClass", wrapAsPlaceholder("current.mapper.name"))));
        }
        ClassMetaModel returnClassMetaModelOfMethod = mapperConfigurationByMethodName.getTargetMetaModel();

        return new MethodInCurrentClassAssignExpression(innerMethodName,
            List.of(methodArgumentExpression),
            returnClassMetaModelOfMethod);
    }
}

package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.parser.InnerMethodByNameExtractor.getMapperConfiguration;

import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

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
        MapperConfiguration mapperConfigurationByMethodName = getMapperConfiguration(
            mapperConfigurationParserContext, sourceExpressionParserContext, innerMethodName,
            methodArgumentExpression, this);
        ClassMetaModel returnClassMetaModelOfMethod = mapperConfigurationByMethodName.getTargetMetaModel();

        return new MethodInCurrentClassAssignExpression(innerMethodName,
            List.of(methodArgumentExpression),
            returnClassMetaModelOfMethod);
    }
}

package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;

import java.util.Map;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;

@UtilityClass
public class InnerMethodByNameExtractor {

    public static MapperConfiguration getMapperConfiguration(MapperConfigurationParserContext mapperConfigurationParserContext,
        SourceExpressionParserContext sourceExpressionParserContext, String innerMethodName,
        ValueToAssignExpression methodArgumentExpression, SourceExpressionParser fromSourceExpressionParser) {

        MapperConfiguration mapperConfigurationByMethodName = sourceExpressionParserContext.getMapperGenerateConfiguration()
            .getMapperConfigurationByMethodName(innerMethodName);

        if (mapperConfigurationByMethodName == null) {
            mapperConfigurationParserContext.throwParseException(createMessagePlaceholder("cannot.find.method.with.arguments",
                Map.of("methodName", innerMethodName,
                    "classesTypes", fromSourceExpressionParser.generateCodeMetadataFor(methodArgumentExpression, mapperConfigurationParserContext)
                        .getReturnClassModel().getTypeDescription(),
                    "givenClass", wrapAsPlaceholder("current.mapper.name"))));
        }
        return mapperConfigurationByMethodName;
    }
}

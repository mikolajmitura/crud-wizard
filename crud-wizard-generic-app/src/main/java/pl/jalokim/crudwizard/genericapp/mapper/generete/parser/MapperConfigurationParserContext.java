package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.utils.collection.Elements;

@RequiredArgsConstructor
public class MapperConfigurationParserContext {

    private final ApplicationContext applicationContext;
    @Getter
    private final MapperGenerateConfiguration mapperGenerateConfiguration;

    @Setter
    private SourceExpressionParser currentSourceExpressionParser;

    @Getter
    private SourceExpressionParserContext sourceExpressionParserContext;

    @Getter
    private PropertiesOverriddenMapping propertyOverriddenMapping;

    @Getter
    private int currentLineNumber;
    @Getter
    private Integer columnNumber;

    private final List<Throwable> errors = new ArrayList<>();

    @Getter
    @Setter
    private MapperConfiguration currentMapperConfiguration;

    public void nextLine(int lineNumber) {
        columnNumber = null;
        currentLineNumber = lineNumber;
        sourceExpressionParserContext = null;
        currentSourceExpressionParser = applicationContext.getBean(InitSourceExpressionParser.class);
    }

    public void nextColumnNumber() {
        columnNumber++;
    }

    public void previousColumnNumber() {
        columnNumber--;
    }

    public void setInitColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public void throwParseException(MessagePlaceholder messagePlaceholder) {
        throwParseExceptionWithRawMessage(messagePlaceholder.translateMessage());
    }

    public void throwParseException(String translateKey, String... args) {
        throwParseException(createMessagePlaceholder(translateKey, args));
    }

    public void addException(Throwable throwable) {
        if (throwable instanceof EntryMappingParseException) {
            errors.add(throwable);
        } else {
            errors.add(createParseException(throwable.getMessage()));
        }
    }

    public void throwParseExceptionWithRawMessage(String rawMessage) {
        throw createParseException(rawMessage);
    }

    public EntryMappingParseException createParseException(MessagePlaceholder messagePlaceholder) {
        return createParseException(messagePlaceholder.translateMessage());
    }

    public EntryMappingParseException createParseException(String message) {
        MapperContextEntryError mapperContextEntryError = createMapperContextEntryError(message);
        return new EntryMappingParseException(mapperContextEntryError);
    }

    private MapperContextEntryError createMapperContextEntryError(String message) {
        return MapperContextEntryError.builder()
            .entryIndex(currentLineNumber)
            .columnNumber(columnNumber)
            .errorReason(message)
            .build();
    }

    public void sourceExpressionParse() {
        ValueToAssignExpression valueToAssignExpression = currentSourceExpressionParser
            .mainParse(this, sourceExpressionParserContext);

        propertyOverriddenMapping.getValueMappingStrategy().add(valueToAssignExpression);
    }

    public void throwExceptionWhenHasErrors() {
        if (CollectionUtils.isNotEmpty(errors)) {
            throw new TechnicalException(Elements.elements(errors)
                .concatWithNewLines());
        }
    }

    public void initSourceExpressionContext(String wholeExpressionText, MapperConfiguration mapperConfiguration,
        PropertiesOverriddenMapping propertyOverriddenMapping, ClassMetaModel targetFieldClassMetaModel) {

        this.propertyOverriddenMapping = propertyOverriddenMapping;
        sourceExpressionParserContext = new SourceExpressionParserContext(wholeExpressionText, this,
            mapperConfiguration, targetFieldClassMetaModel);
    }

    public FieldMetaResolverConfiguration getFieldMetaResolverForRawSource() {
        return mapperGenerateConfiguration.getFieldMetaResolverForRawSource();
    }

    public ClassMetaModel getPathVariablesClassModel() {
        return mapperGenerateConfiguration.getPathVariablesClassModel();
    }

    public ClassMetaModel getRequestParamsClassModel() {
        return mapperGenerateConfiguration.getRequestParamsClassModel();
    }
}

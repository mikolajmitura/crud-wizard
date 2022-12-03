package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.replaceAllWithEmpty;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.EntryMappingParseException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.EntryMappingParseException.ErrorSource;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.MapperConfigurationParserContext;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.FieldMetaModelExtractor;
import pl.jalokim.utils.collection.Elements;

@Component
@RequiredArgsConstructor
@Slf4j
public class PropertiesOverriddenMappingResolver {

    private final ApplicationContext applicationContext;

    public void populateMappingEntriesToConfiguration(MapperGenerateConfiguration mapperGenerateConfiguration,
        List<MappingEntryModel> rootMapperMappingEntries, Map<String, List<MappingEntryModel>> mappingEntriesByMethodName) {

        MapperConfigurationParserContext parserContext = new MapperConfigurationParserContext(applicationContext, mapperGenerateConfiguration);

        var fieldMetaResolverForRawTarget = mapperGenerateConfiguration.getFieldMetaResolverForRawTarget();

        parserContext.setCurrentMapperConfiguration(mapperGenerateConfiguration.getRootConfiguration());
        elements(rootMapperMappingEntries)
            .forEachWithIndex((entryIndex, mappingEntry) ->
                parseMappingEntry(parserContext, mappingEntry,
                    entryIndex, fieldMetaResolverForRawTarget));

        mappingEntriesByMethodName.forEach(
            (methodName, mappingEntries) -> {
                MapperConfiguration methodMappingEntries = mapperGenerateConfiguration.getMapperConfigurationByMethodName(methodName);
                parserContext.setCurrentMapperConfiguration(methodMappingEntries);
                elements(mappingEntries)
                    .forEachWithIndex((entryIndex, mappingEntry) ->
                        parseMappingEntry(parserContext, mappingEntry,
                            entryIndex, fieldMetaResolverForRawTarget));
            }
        );

        parserContext.throwExceptionWhenHasErrors();
    }

    public void parseMappingEntry(MapperConfigurationParserContext parserContext,
        MappingEntryModel mappingEntry, int entryIndex,
        FieldMetaResolverConfiguration fieldMetaResolverForRawTarget) {

        try {
            tryParseEntry(parserContext, mappingEntry, entryIndex, fieldMetaResolverForRawTarget);
        } catch (EntryMappingParseException entryMappingParseException) {
            parserContext.addException(entryMappingParseException);
            log.debug("parse entry exception: {}", entryMappingParseException.getMessage());
        } catch (Exception ex) {
            parserContext.addException(ex);
            log.warn("parse unexpected exception", ex);
        }
    }

    private void tryParseEntry(MapperConfigurationParserContext parserContext,
        MappingEntryModel mappingEntry, int entryIndex, FieldMetaResolverConfiguration fieldMetaResolverForRawTarget) {

        MapperConfiguration mapperConfiguration = parserContext.getCurrentMapperConfiguration();

        ClassMetaModel targetMetaModel = mapperConfiguration.getTargetMetaModel();
        if (targetMetaModel.isOnlyRawClassModel()) {
            targetMetaModel = ClassMetaModelFactory.generateGenericClassMetaModel(targetMetaModel.getRealClass(), fieldMetaResolverForRawTarget);
        }

        parserContext.nextEntry(entryIndex);
        String targetExpression = mappingEntry.getTargetAssignPath();
        String sourceExpression = mappingEntry.getSourceAssignExpression();

        ClassMetaModel targetFieldClassMetaModel = targetMetaModel;

        PropertiesOverriddenMapping currentOverriddenMapping = parserContext.getCurrentMapperConfiguration().getPropertyOverriddenMapping();
        if (StringUtils.isNotBlank(targetExpression)) {
            try {
                targetFieldClassMetaModel = FieldMetaModelExtractor.extractFieldMetaModel(targetMetaModel, targetExpression)
                    .getFieldType();
            } catch (TechnicalException ex) {
                parserContext.throwParseExceptionWithRawMessage(ErrorSource.TARGET_EXPRESSION, ex.getMessage());
            }

            List<String> pathParts = Elements.bySplitText(replaceAllWithEmpty(targetExpression, " "), "\\.")
                .asList();

            for (String pathPart : pathParts) {
                Map<String, PropertiesOverriddenMapping> mappingsByPropertyName = currentOverriddenMapping.getMappingsByPropertyName();
                currentOverriddenMapping = mappingsByPropertyName
                    .computeIfAbsent(pathPart, (fieldName) -> PropertiesOverriddenMapping.builder().build());
            }
        }

        parserContext.initSourceExpressionContext(sourceExpression, mapperConfiguration,
            currentOverriddenMapping, targetFieldClassMetaModel);

        parserContext.sourceExpressionParse();
    }
}

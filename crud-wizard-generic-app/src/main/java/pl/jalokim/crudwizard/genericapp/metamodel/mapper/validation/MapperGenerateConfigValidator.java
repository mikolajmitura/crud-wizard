package pl.jalokim.crudwizard.genericapp.metamodel.mapper.validation;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath.PropertyPathBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MappingEntryModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMappingResolver;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.EntryMappingParseException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.EntryMappingParseException.ErrorSource;
import pl.jalokim.crudwizard.genericapp.mapper.generete.parser.MapperConfigurationParserContext;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperGenerateConfigValidator implements BaseConstraintValidator<MapperGenerateConfigCheck, MapperGenerateConfigurationDto> {

    private final MapperGenerateConfigurationMapper mapperGenerateConfigurationMapper;
    private final PropertiesOverriddenMappingResolver propertiesOverriddenMappingResolver;
    private final ApplicationContext applicationContext;

    @Override
    public boolean isValidValue(MapperGenerateConfigurationDto mapperGenerateConfigurationDto, ConstraintValidatorContext context) {
        AtomicBoolean isValid = new AtomicBoolean(true);

        if (mapperGenerateConfigurationDto.getRootConfiguration() != null) {
            var createEndpointMetaModelDto = getTemporaryMetaModelContext().getCreateEndpointMetaModelDto();

            MapperGenerateConfiguration mapperGenerateConfiguration;
            try {
                mapperGenerateConfiguration = mapperGenerateConfigurationMapper.mapConfiguration(
                    mapperGenerateConfigurationDto, createEndpointMetaModelDto.getPathParams(), createEndpointMetaModelDto.getQueryArguments());
            } catch (Exception ex) {
                customMessage(context, "{MapperGenerateConfigurationMapper.create.config.problem}");
                log.error("unexpected error", ex);
                return false;
            }

            MapperConfigurationDto mapperConfigurationDto = mapperGenerateConfigurationDto.getRootConfiguration();
            MapperConfiguration mapperConfiguration = mapperGenerateConfiguration.getRootConfiguration();

            validateSourceExpression(mapperGenerateConfiguration, isValid,
                mapperConfigurationDto, mapperConfiguration, context,
                PropertyPath.builder().addNextProperty("rootConfiguration"));

            if (isNotEmpty(mapperGenerateConfigurationDto.getSubMappersAsMethods())) {

                elements(mapperGenerateConfigurationDto.getSubMappersAsMethods())
                    .forEachWithIndex((index, mapperMethodConfigDto) -> {

                        MapperConfiguration mapperMethodConfig = mapperGenerateConfiguration.getMapperConfigurationByMethodName(
                            mapperMethodConfigDto.getName());

                        validateSourceExpression(mapperGenerateConfiguration, isValid,
                            mapperMethodConfigDto, mapperMethodConfig, context,
                            PropertyPath.builder().addNextPropertyAndIndex("subMappersAsMethods", index));
                    });
            }

            if (isValid.get()) {
                // TODO #1 try generate mapper code ##_1 #NOW
            }
        }

        return isValid.get();
    }

    private void validateSourceExpression(MapperGenerateConfiguration mapperGenerateConfiguration, AtomicBoolean isValid,
        MapperConfigurationDto mapperConfigurationDto, MapperConfiguration mapperConfiguration,
        ConstraintValidatorContext context, PropertyPathBuilder propertyPathBuilderTemplate) {

        elements(mapperConfigurationDto.getPropertyOverriddenMapping())
            .forEachWithIndex((index, propertiesOverriddenMappingDto) -> {

                if (propertiesOverriddenMappingDto.getTargetAssignPath() == null || propertiesOverriddenMappingDto.getSourceAssignExpression() == null) {
                    return;
                }

                MapperConfigurationParserContext parserContext = new MapperConfigurationParserContext(applicationContext, mapperGenerateConfiguration);
                var fieldMetaResolverForRawTarget = mapperGenerateConfiguration.getFieldMetaResolverForRawTarget();
                parserContext.setCurrentMapperConfiguration(mapperConfiguration);

                MappingEntryModel mappingEntryModel = new MappingEntryModel(propertiesOverriddenMappingDto.getTargetAssignPath(),
                    propertiesOverriddenMappingDto.getSourceAssignExpression());
                propertiesOverriddenMappingResolver.parseMappingEntry(parserContext, mappingEntryModel, 0, fieldMetaResolverForRawTarget);

                List<Throwable> errors = parserContext.getErrors();
                if (isNotEmpty(errors)) {

                    errors.forEach(error -> {

                        String message = error.getMessage();
                        String expressionFieldName = null;
                        if (error instanceof EntryMappingParseException) {
                            EntryMappingParseException exception = (EntryMappingParseException) error;
                            message = exception.getMapperContextEntryError().getMessageWithoutEntryIndex();
                            expressionFieldName = resolveFieldName(exception.getErrorType());
                        }

                        PropertyPathBuilder propertyPathBuilder = propertyPathBuilderTemplate.copy()
                            .addNextPropertyAndIndex("propertyOverriddenMapping", index);

                        if (expressionFieldName != null) {
                            propertyPathBuilder = propertyPathBuilder.addNextProperty(expressionFieldName);
                        }

                        customMessage(context, message, propertyPathBuilder.build());
                    });

                    isValid.set(false);
                }
            });
    }

    private String resolveFieldName(ErrorSource errorSource) {
        switch (errorSource) {
            case SOURCE_EXPRESSION:
                return  "sourceAssignExpression";
            case TARGET_EXPRESSION:
                return "targetAssignPath";
            default:
                return null;
        }
    }
}

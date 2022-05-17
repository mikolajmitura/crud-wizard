package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelConstants.STRING_MODEL;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.ExpressionSourcesUtils.convertAssignExpressionsToMethodArguments;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator.getGeneratedNewMethodOrGetCreatedEarlier;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression.NULL_ASSIGN;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.EnumsMappingMethodResolver;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.EnumEntriesMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.EnumClassMetaModelValidator;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.utils.template.TemplateAsText;

@Component
@RequiredArgsConstructor
class SimpleTargetAssignResolver {

    private static final String CASE_TEMPLATE = "\t\t\tcase ${from}: return ${to};";

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final AssignExpressionAsTextResolver assignExpressionAsTextResolver;

    void generateMapperMethodWhenMapToSimpleType(List<MapperArgumentMethodModel> methodArguments,
        MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, TargetFieldMetaData returnMethodMetaData) {

        var currentNodeOverriddenMappings = methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode();
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();

        if (currentNodeOverriddenMappings.isEmpty()) {
            currentNodeOverriddenMappings = methodArguments.stream()
                .map(argument -> new RawJavaCodeAssignExpression(argument.getArgumentType(), "sourceObject"))
                .collect(Collectors.toList());
        }

        assignValueForSimpleField(methodGeneratorArgument,
            assignExpressionForFieldReference, returnMethodMetaData,
            currentNodeOverriddenMappings);

        String expression = assignExpressionAsTextResolver.getExpressionForAssignWhenExists(methodGeneratorArgument,
            assignExpressionForFieldReference,
            returnMethodMetaData,
            null);

        if (expression != null) {
            methodBuilder.lastLine("return " + expression);
        }
    }

    void assignValueForSimpleField(MapperMethodGeneratorArgument methodGeneratorArgument,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData,
        List<ValueToAssignExpression> foundAssignExpressionsForField) {

        String fieldName = targetFieldMetaData.getFieldName();
        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        if (assignExpressionForFieldReference.get() == null) {
            if (foundAssignExpressionsForField.size() > 1) {
                mapperGeneratedCodeMetadata.throwMappingError(
                    createMessagePlaceholder("mapper.found.to.many.mappings.for.simple.type",
                        currentPath.nextNode(fieldName).getFullPath())
                );
            } else if (foundAssignExpressionsForField.size() == 1) {

                ValueToAssignExpression valueToAssignExpression = foundAssignExpressionsForField.get(0);
                ValueToAssignCodeMetadata valueToAssignCodeMetadata = valueToAssignExpression
                    .generateCodeMetadata(methodGeneratorArgument.getMapperGeneratedCodeMetadata());
                ClassMetaModel sourceClassModel = valueToAssignCodeMetadata.getReturnClassModel();

                if (!sourceClassModel.isTheSameMetaModel(targetFieldClassMetaModel)
                    && sourceClassModel.isEnumTypeOrJavaEnum()
                    && targetFieldClassMetaModel.isEnumTypeOrJavaEnum()) {

                    assignExpressionForFieldReference.set(createAsEnumAssign(methodGeneratorArgument,
                        targetFieldClassMetaModel, sourceClassModel, valueToAssignExpression, targetFieldMetaData));
                } else {
                    assignExpressionForFieldReference.set(valueToAssignExpression);
                    if (!canConvert(sourceClassModel, targetFieldClassMetaModel)) {
                        if (String.class.equals(sourceClassModel.getRealClass()) && targetFieldClassMetaModel.isGenericMetamodelEnum()) {
                            assignExpressionForFieldReference.set(new BySpringBeanMethodAssignExpression(EnumClassMetaModelValidator.class,
                                "enumClassMetaModelValidator", "getEnumValueWhenIsValid",
                                List.of(new RawJavaCodeAssignExpression(STRING_MODEL, wrapTextWith(targetFieldClassMetaModel.getName(), "\"")),
                                    valueToAssignExpression,
                                    new RawJavaCodeAssignExpression(STRING_MODEL, wrapTextWith(currentPath.nextNode(fieldName).getFullPath(), "\"")))
                            ));
                        }
                    }
                }
            }
        }
    }

    private ValueToAssignExpression createAsEnumAssign(MapperMethodGeneratorArgument methodGeneratorArgument,
        ClassMetaModel targetFieldClassMetaModel, ClassMetaModel sourceClassModel,
        ValueToAssignExpression valueToAssignExpression, TargetFieldMetaData returnMethodMetaData) {

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        List<MapperArgumentMethodModel> mapperArgumentMethodModels = convertAssignExpressionsToMethodArguments(mapperGeneratedCodeMetadata,
            List.of(valueToAssignExpression));

        List<MapperConfiguration> foundMapperConfigurations = methodGeneratorArgument.getMapperGenerateConfiguration()
            .findMapperConfigurationBy(sourceClassModel, targetFieldClassMetaModel);

        MapperConfiguration mapperConfigurationForEnums;

        if (foundMapperConfigurations.isEmpty()) {
            mapperConfigurationForEnums = methodGeneratorArgument.getMapperConfiguration();
        } else if (foundMapperConfigurations.size() == 1) {
            mapperConfigurationForEnums = foundMapperConfigurations.get(0);
        } else {
            methodGeneratorArgument.getMapperGeneratedCodeMetadata()
                .throwMappingError(createMessagePlaceholder("mapper.found.to.many.methods",
                    elements(foundMapperConfigurations).asConcatText(", "),
                    returnMethodMetaData.getFieldNameNodePath().getFullPath()));
            return null;
        }

        String methodName;

        if (mapperConfigurationForEnums.isForMappingEnums() && mapperConfigurationForEnums.getName() != null) {
            methodName = mapperConfigurationForEnums.getName();
        } else {
            methodName = createMethodName(mapperArgumentMethodModels, targetFieldClassMetaModel);
        }

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .returnClassMetaModel(targetFieldClassMetaModel)
            .methodReturnType(targetFieldClassMetaModel.getJavaGenericTypeInfo())
            .methodArguments(mapperArgumentMethodModels)
            .methodName(methodName)
            .generated(methodGeneratorArgument.isGenerated())
            .parentMethodMetadata(methodGeneratorArgument.getParentMethodCodeMetadata());

        EnumEntriesMapping enumEntriesMapping = mapperConfigurationForEnums.getEnumEntriesMapping();
        Map<String, String> overriddenEnumMappings = enumEntriesMapping.getTargetEnumBySourceEnum();
        methodBuilder.methodTemplateResolver(new EnumsMappingMethodResolver(resolveDefaultEnumMapping(
            targetFieldClassMetaModel, enumEntriesMapping.getWhenNotMappedEnum())));

        var allTargetEnums = getEnumValues(targetFieldClassMetaModel);
        var allSourceEnums = getEnumValues(sourceClassModel);

        allSourceEnums.forEach((sourceEnumValue, sourceFullEnumValue) -> {

            if (!enumEntriesMapping.getIgnoredSourceEnum().contains(sourceEnumValue)) {
                EnumTypeMetaData targetEnumTypeMetaData;
                String foundOverriddenTargetEnumValue = overriddenEnumMappings.get(sourceEnumValue);
                if (foundOverriddenTargetEnumValue != null) {
                    targetEnumTypeMetaData = allTargetEnums.get(foundOverriddenTargetEnumValue);
                } else {
                    targetEnumTypeMetaData = allTargetEnums.get(sourceEnumValue);
                }
                if (targetEnumTypeMetaData == null) {
                    methodGeneratorArgument.getMapperGeneratedCodeMetadata().
                        addError(createMessagePlaceholder("mapper.cannot.map.enum.value",
                            sourceEnumValue, sourceClassModel.getTypeDescription(),
                            targetFieldClassMetaModel.getTypeDescription(),
                            returnMethodMetaData.getFieldNameNodePath().getFullPath()));
                } else {
                    TemplateAsText templateAsText = TemplateAsText.fromText(CASE_TEMPLATE)
                        .overrideVariable("to", targetEnumTypeMetaData.getAsMappingTo())
                        .overrideVariable("from", sourceFullEnumValue.getAsMappingFrom());
                    methodBuilder.nextMappingCodeLine(templateAsText.getCurrentTemplateText());
                }
            }
        });
        MethodCodeMetadata newMethodOrEarlier = getGeneratedNewMethodOrGetCreatedEarlier(mapperGeneratedCodeMetadata,
            methodGeneratorArgument.getParentMethodCodeMetadata(),
            methodBuilder.build());

        return new MethodInCurrentClassAssignExpression(newMethodOrEarlier.getMethodName(),
            List.of(valueToAssignExpression), targetFieldClassMetaModel);
    }

    private Map<String, EnumTypeMetaData> getEnumValues(ClassMetaModel classMetaModel) {
        if (classMetaModel.isGenericMetamodelEnum()) {
            EnumClassMetaModel enumClassMetaModel = classMetaModel.getEnumClassMetaModel();
            return elements(enumClassMetaModel.getEnumValues())
                .asMap(Function.identity(),
                    enumValue -> {
                        String wrapWithQuoteEnumValue = StringUtils.wrap(enumValue, '"');
                        return new EnumTypeMetaData(wrapWithQuoteEnumValue, wrapWithQuoteEnumValue);
                    });
        }

        return elements(classMetaModel.getRealClass().getEnumConstants())
            .map(enumEntry -> (Enum<?>) enumEntry)
            .asMap(Enum::name,
                enumEntry ->
                    new EnumTypeMetaData(enumEntry.name(),
                        enumEntry.getClass().getCanonicalName() + "." + enumEntry.name())
            );
    }

    private String resolveDefaultEnumMapping(ClassMetaModel targetMetaModel, String whenNotMappedEnumFromConfig) {
        if (NULL_ASSIGN.equals(whenNotMappedEnumFromConfig)) {
            return "return " + NULL_ASSIGN;
        }
        if (whenNotMappedEnumFromConfig.trim().contains("throw ")) {
            return whenNotMappedEnumFromConfig;
        }
        return "return " + enumValueAsFullPath(targetMetaModel, whenNotMappedEnumFromConfig);
    }

    private String enumValueAsFullPath(ClassMetaModel classMetaModel, String shortEnumValue) {
        if (classMetaModel.isRealClassEnum()) {
            return classMetaModel.getRealClass().getCanonicalName() + "." + shortEnumValue;
        }
        return shortEnumValue;
    }

    @Value
    private static class EnumTypeMetaData {

        String asMappingFrom;
        String asMappingTo;
    }

    private boolean canConvert(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        return genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel) != null;
    }

    private static String wrapTextWith(String text, String wrapWith) {
        return wrapWith + text + wrapWith;
    }
}

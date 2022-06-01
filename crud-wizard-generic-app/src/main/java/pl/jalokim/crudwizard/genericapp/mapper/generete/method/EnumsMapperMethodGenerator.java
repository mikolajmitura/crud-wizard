package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.AssignExpressionAsTextResolver.getInMethodPartMessage;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator.getGeneratedNewMethodOrGetCreatedEarlier;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression.NULL_ASSIGN;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.EnumsMappingMethodResolver;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.EnumEntriesMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.utils.template.TemplateAsText;

@Component
public class EnumsMapperMethodGenerator {

    private static final String CASE_TEMPLATE = "\t\t\tcase ${from}: return ${to};";

    public MethodCodeMetadata creteEnumsMappingMethod(MapperMethodGeneratorArgument methodGeneratorArgument) {

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MapperConfiguration mapperConfiguration = methodGeneratorArgument.getMapperConfiguration();

        List<MapperArgumentMethodModel> mapperArgumentMethodModels = methodGeneratorArgument.getMapperMethodArguments();
        ClassMetaModel sourceFieldClassMetaModel = mapperArgumentMethodModels.get(0).getArgumentType();
        ClassMetaModel targetFieldClassMetaModel = methodGeneratorArgument.getTargetMetaModel();

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .returnClassMetaModel(targetFieldClassMetaModel)
            .methodReturnType(targetFieldClassMetaModel.getJavaGenericTypeInfo())
            .methodArguments(mapperArgumentMethodModels)
            .methodName(methodGeneratorArgument.getMethodName())
            .generated(methodGeneratorArgument.isGenerated())
            .parentMethodMetadata(methodGeneratorArgument.getParentMethodCodeMetadata());

        EnumEntriesMapping enumEntriesMapping = mapperConfiguration.getEnumEntriesMapping();
        Map<String, String> overriddenEnumMappings = enumEntriesMapping.getTargetEnumBySourceEnum();
        methodBuilder.methodTemplateResolver(new EnumsMappingMethodResolver(resolveDefaultEnumMapping(
            targetFieldClassMetaModel, enumEntriesMapping.getWhenNotMappedEnum())));

        var allTargetEnums = getEnumValues(targetFieldClassMetaModel);
        var allSourceEnums = getEnumValues(sourceFieldClassMetaModel);

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
                    methodGeneratorArgument.getMapperGeneratedCodeMetadata().addError(
                        createMessagePlaceholder("mapper.cannot.map.enum.value",
                            sourceEnumValue, sourceFieldClassMetaModel.getTypeDescription(),
                            targetFieldClassMetaModel.getTypeDescription(),
                            methodGeneratorArgument.getCurrentPath().getFullPath(),
                            getInMethodPartMessage(methodGeneratorArgument)));
                } else {
                    TemplateAsText templateAsText = TemplateAsText.fromText(CASE_TEMPLATE)
                        .overrideVariable("to", targetEnumTypeMetaData.getAsMappingTo())
                        .overrideVariable("from", sourceFullEnumValue.getAsMappingFrom());
                    methodBuilder.nextMappingCodeLine(templateAsText.getCurrentTemplateText());
                }
            }
        });

        return getGeneratedNewMethodOrGetCreatedEarlier(mapperGeneratedCodeMetadata,
            methodGeneratorArgument.getParentMethodCodeMetadata(),
            methodBuilder.build());
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
}

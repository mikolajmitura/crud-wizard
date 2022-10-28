package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperGenerateConstants.SOURCE_OBJECT_VAR_NAME;
import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;

class ExpressionSourcesUtils {

    static List<MapperArgumentMethodModel> convertAssignExpressionsToMethodArguments(MapperCodeMetadata mapperGeneratedCodeMetadata,
        List<ValueToAssignExpression> methodArgumentsExpressions) {

        return elements(methodArgumentsExpressions)
            .mapWithIndex((index, expression) -> new MapperArgumentMethodModel(
                methodArgumentsExpressions.size() == 1 ? SOURCE_OBJECT_VAR_NAME : "argument" + index,
                expression.generateCodeMetadata(mapperGeneratedCodeMetadata).getReturnClassModel(),
                expression))
            .asList();
    }

    static List<ValueToAssignExpression> getOverriddenExpressionsOrFindByFieldName(MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData) {

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        List<ValueToAssignExpression> overriddenPropertyStrategiesByFieldName = targetFieldMetaData.getOverriddenPropertyStrategiesByForField();

        List<ValueToAssignExpression> methodArgumentsExpressions = new ArrayList<>();
        if (isNotEmpty(overriddenPropertyStrategiesByFieldName)) {
            methodArgumentsExpressions.addAll(overriddenPropertyStrategiesByFieldName);
        } else {
            methodArgumentsExpressions.addAll(findValueExpressionsInMethodArgumentsByFieldName(methodGeneratorArgument,
                mapperGeneratedCodeMetadata, targetFieldMetaData));

            if (methodArgumentsExpressions.isEmpty()) {
                methodArgumentsExpressions.addAll(findValueExpressionsByFieldName(
                    methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode(),
                    mapperGeneratedCodeMetadata, targetFieldMetaData));
            }
        }
        if (isEmpty(methodArgumentsExpressions) && containsObjectSourceInAnyNestedExpressions(methodGeneratorArgument.getMapperGeneratedCodeMetadata(),
            targetFieldMetaData.getPropertiesOverriddenMappingForField())) {

            methodArgumentsExpressions.addAll(elements(methodGeneratorArgument.getMapperMethodArguments())
            .map(argument -> new RawJavaCodeAssignExpression(argument.getArgumentType(), argument.getArgumentName()))
            .asList());
        }
        return methodArgumentsExpressions;
    }

    private static boolean containsObjectSourceInAnyNestedExpressions(MapperCodeMetadata mapperGeneratedCodeMetadata,
        PropertiesOverriddenMapping propertiesOverriddenMapping) {
        AtomicBoolean foundSourceObjectInExpressions = new AtomicBoolean();
        containsObjectSourceInAnyNestedExpressions(foundSourceObjectInExpressions, mapperGeneratedCodeMetadata, propertiesOverriddenMapping);
        return foundSourceObjectInExpressions.get();
    }

    private static void containsObjectSourceInAnyNestedExpressions(AtomicBoolean foundObjectSource,
        MapperCodeMetadata mapperGeneratedCodeMetadata, PropertiesOverriddenMapping propertiesOverriddenMapping) {
        if (foundObjectSource.get()) {
            return;
        }
        var mappingsByPropertyName = propertiesOverriddenMapping.getMappingsByPropertyName();
        if (mappingsByPropertyName != null) {
            for (var entry : mappingsByPropertyName.entrySet()) {
                for (ValueToAssignExpression valueToAssignExpression : entry.getValue().getValueMappingStrategy()) {
                    ValueToAssignCodeMetadata valueToAssignCodeMetadata = valueToAssignExpression.generateCodeMetadata(mapperGeneratedCodeMetadata);
                    String fullValueExpression = valueToAssignCodeMetadata.getFullValueExpression();
                    if (fullValueExpression.contains(SOURCE_OBJECT_VAR_NAME)) {
                        foundObjectSource.set(true);
                        return;
                    }
                }
                containsObjectSourceInAnyNestedExpressions(foundObjectSource, mapperGeneratedCodeMetadata, entry.getValue());
            }
        }
    }

    private static List<ValueToAssignExpression> findValueExpressionsInMethodArgumentsByFieldName(MapperMethodGeneratorArgument methodGeneratorArgument,
        MapperCodeMetadata mapperGeneratedCodeMetadata,
        TargetFieldMetaData targetFieldMetaData) {

        List<ValueToAssignExpression> foundExpressions = new ArrayList<>();
        for (MapperArgumentMethodModel mapperMethodArgument : methodGeneratorArgument.getMapperMethodArguments()) {
            var sourceMetaModel = mapperMethodArgument.getArgumentType();
            addWhenFoundByField(mapperGeneratedCodeMetadata, targetFieldMetaData,
                foundExpressions, sourceMetaModel, mapperMethodArgument.getArgumentName());
        }
        return foundExpressions;
    }

    private static void addWhenFoundByField(MapperCodeMetadata mapperGeneratedCodeMetadata, TargetFieldMetaData targetFieldMetaData,
        List<ValueToAssignExpression> foundExpressions, ClassMetaModel sourceMetaModel, String argumentName) {

        String fieldName = targetFieldMetaData.getFieldName();
        if (sourceMetaModel.isSimpleType()) {
            mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder("cannot.get.field.from.simple.field",
                fieldName, sourceMetaModel.getCanonicalNameOfRealClass(), targetFieldMetaData.getFieldNameNodePath()));
        } else {
            FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
            if (fieldFromSource != null) {
                foundExpressions.add(new FieldsChainToAssignExpression(
                    sourceMetaModel, argumentName, List.of(fieldFromSource)));
            }
        }
    }

    private static List<ValueToAssignExpression> findValueExpressionsByFieldName(List<ValueToAssignExpression> givenExpressions,
        MapperCodeMetadata mapperGeneratedCodeMetadata,
        TargetFieldMetaData targetFieldMetaData) {

        List<ValueToAssignExpression> foundExpressions = new ArrayList<>();
        for (ValueToAssignExpression expression : givenExpressions) {
            ValueToAssignCodeMetadata valueToAssignCodeMetadata = expression.generateCodeMetadata(mapperGeneratedCodeMetadata);
            var sourceMetaModel = valueToAssignCodeMetadata.getReturnClassModel();
            addWhenFoundByField(mapperGeneratedCodeMetadata, targetFieldMetaData, foundExpressions,
                sourceMetaModel, valueToAssignCodeMetadata.getFullValueExpression());
        }
        return foundExpressions;
    }
}

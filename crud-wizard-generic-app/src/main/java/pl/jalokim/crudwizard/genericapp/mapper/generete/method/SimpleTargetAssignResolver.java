package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperGenerateConstants.SOURCE_OBJECT_VAR_NAME;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.ExpressionSourcesUtils.convertAssignExpressionsToMethodArguments;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator.getGeneratedNewMethodOrGetCreatedEarlier;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.STRING_MODEL;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.EnumClassMetaModelValidator;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Component
@RequiredArgsConstructor
public class SimpleTargetAssignResolver {

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final AssignExpressionAsTextResolver assignExpressionAsTextResolver;
    private final EnumsMapperMethodGenerator enumsMapperMethodGenerator;

    void generateMapperMethodWhenMapToSimpleType(List<MapperArgumentMethodModel> methodArguments,
        MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, TargetFieldMetaData returnMethodMetaData) {

        var currentNodeOverriddenMappings = methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode();
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();

        if (currentNodeOverriddenMappings.isEmpty()) {
            currentNodeOverriddenMappings = methodArguments.stream()
                .map(argument -> new RawJavaCodeAssignExpression(argument.getArgumentType(), SOURCE_OBJECT_VAR_NAME))
                .collect(Collectors.toList());
        }

        assignValueForSimpleField(methodGeneratorArgument,
            assignExpressionForFieldReference, returnMethodMetaData,
            currentNodeOverriddenMappings);

        String expression = assignExpressionAsTextResolver.getExpressionForAssignWhenExists(methodGeneratorArgument,
            assignExpressionForFieldReference,
            returnMethodMetaData,
            null, false);

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
                    createMessagePlaceholder("mapper.found.to.many.mappings.for.simple.type")
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

        List<MethodMetadataMapperConfig> foundMatchedInnerNotGeneratedMethods = mapperGeneratedCodeMetadata
            .findMatchNotGeneratedMethod(targetFieldClassMetaModel, sourceClassModel);

        String methodName;

        if (foundMatchedInnerNotGeneratedMethods.isEmpty()) {
            MethodCodeMetadata creteEnumsMappingMethod = enumsMapperMethodGenerator
                .creteEnumsMappingMethod(methodGeneratorArgument.toBuilder()
                    .targetMetaModel(targetFieldClassMetaModel)
                    .mapperMethodArguments(mapperArgumentMethodModels)
                    .methodName(createMethodName(mapperArgumentMethodModels, targetFieldClassMetaModel))
                    .currentPath(returnMethodMetaData.getFieldNameNodePath())
                    .parentMapperMethodGeneratorArgument(methodGeneratorArgument)
                    .build());

            MethodCodeMetadata newMethodOrEarlier = getGeneratedNewMethodOrGetCreatedEarlier(mapperGeneratedCodeMetadata,
                methodGeneratorArgument.getParentMethodCodeMetadata(),
                creteEnumsMappingMethod);

            methodName = newMethodOrEarlier.getMethodName();

        } else if (foundMatchedInnerNotGeneratedMethods.size() == 1) {
            methodName = foundMatchedInnerNotGeneratedMethods.get(0).getMethodName();
        } else {
            methodGeneratorArgument.getMapperGeneratedCodeMetadata()
                .throwMappingError(createMessagePlaceholder("mapper.found.to.many.methods",
                    elements(foundMatchedInnerNotGeneratedMethods)
                        .map(MethodMetadataMapperConfig::getMethodName)
                        .asConcatText(", ")));
            return null;
        }

        return new MethodInCurrentClassAssignExpression(methodName,
            List.of(valueToAssignExpression), targetFieldClassMetaModel);
    }

    private boolean canConvert(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        return genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel) != null;
    }

    private static String wrapTextWith(String text, String wrapWith) {
        return wrapWith + text + wrapWith;
    }
}

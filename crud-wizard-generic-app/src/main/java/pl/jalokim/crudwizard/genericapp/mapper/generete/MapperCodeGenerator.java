package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelForMapperHelper.getClassModelInfoForGeneratedCode;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith2Tabs;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.regenerateMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.containsNestedMappings;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.findOverriddenMappingStrategies;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.findOverriddenMappingStrategiesForCurrentNode;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.getPropertiesOverriddenMapping;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.givenFieldIsIgnored;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategyFactory.createWritePropertyStrategy;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isHavingElementsType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WriteToMapStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.service.translator.DefaultSubClassesForAbstractClassesConfig;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.utils.template.TemplateAsText;

@Component
@RequiredArgsConstructor
public class MapperCodeGenerator {

    // TODO #1 mapper task orders
    //  - implement mapping set, list etc and test that
    //  - implement mapping enums and test that
    //  - validation of correctness of mapping during add new endpoint with mappers, and test (not IT)
    //  - generate mapper code and compile it, put to classloader and test that is exists
    //  - generate few mappers code and compile it, put to classloader and map some values by them
    //  - implement use mapper with have input field other than GenericMapperArgument but raw object
    //  - full IT for create new endpoint with mapper with some overridden fields
    //      - invoke that endpoint and verify that mapping was correct

    private final DefaultSubClassesForAbstractClassesConfig defaultSubClassesForAbstractClassesConfig;
    private final GenericObjectsConversionService genericObjectsConversionService;
    private final ConversionService conversionService;
    private final InstanceLoader instanceLoader;

    public String generateMapperCodeMetadata(MapperGenerateConfiguration mapperGenerateConfiguration) {
        MapperConfiguration mapperConfiguration = mapperGenerateConfiguration.getRootConfiguration();
        var sourceMetaModel = mapperConfiguration.getSourceMetaModel();
        var targetMetaModel = mapperConfiguration.getTargetMetaModel();

        MapperCodeMetadata mapperGeneratedCodeMetadata = new MapperCodeMetadata();

        mapperGeneratedCodeMetadata.setMapperClassName(
            String.format("%sTo%sMapper",
                getClassModelInfoForGeneratedCode(sourceMetaModel),
                getClassModelInfoForGeneratedCode(targetMetaModel)
            ));

        mapperGenerateConfiguration.getMapperConfigurationByMethodName()
            .forEach((methodName, subMethodConfiguration) ->
                mapperGeneratedCodeMetadata.addOtherMethod(generateMapperMethod(subMethodConfiguration.getName(), false,
                    List.of(new MapperArgumentMethodModel("sourceObject", subMethodConfiguration.getSourceMetaModel())),
                    subMethodConfiguration.getTargetMetaModel(),
                    mapperGeneratedCodeMetadata,
                    subMethodConfiguration,
                    subMethodConfiguration.getPropertyOverriddenMapping(),
                    mapperGenerateConfiguration,
                    ObjectNodePath.rootNode(),
                    null)
                ));

        mapperGeneratedCodeMetadata.setMainMethodCodeMetadata(
            generateMapperMethod("mainMethod", true,
                List.of(new MapperArgumentMethodModel("sourceObject", sourceMetaModel)), targetMetaModel,
                mapperGeneratedCodeMetadata,
                mapperConfiguration,
                mapperConfiguration.getPropertyOverriddenMapping(),
                mapperGenerateConfiguration,
                ObjectNodePath.rootNode(), null));

        mapperGeneratedCodeMetadata.checkValidationResults();

        return generateMapperCode(mapperGeneratedCodeMetadata);
    }

    private String generateMapperCode(MapperCodeMetadata mapperGeneratedCodeMetadata) {

        MethodCodeMetadata mainMethodCodeMetadata = mapperGeneratedCodeMetadata.getMainMethodCodeMetadata();

        return TemplateAsText.fromClassPath("templates/mapper/mapper-class-template", true)
            .overrideVariable("imports", mapperGeneratedCodeMetadata.getImportsAsText())
            .overrideVariable("staticImports", mapperGeneratedCodeMetadata.getStaticImportsAsText())
            .overrideVariable("mapperClassName", mapperGeneratedCodeMetadata.getMapperClassName())
            .overrideVariable("fields", mapperGeneratedCodeMetadata.getFieldsAsText())
            .overrideVariable("constructorArguments", mapperGeneratedCodeMetadata.getConstructorArgumentsAsText())
            .overrideVariable("fieldsAssignments", mapperGeneratedCodeMetadata.getFieldsAssignmentsAsText())
            .overrideVariable("methodReturnType", mainMethodCodeMetadata.getMethodReturnType())
            .overrideVariable("mappingsCode", mainMethodCodeMetadata.getMappingsCodeAsText())
            .overrideVariable("lastLine", mainMethodCodeMetadata.getLastLine())
            .overrideVariable("otherMapperMethods", mapperGeneratedCodeMetadata.getOtherMapperMethodsAsText())
            .getCurrentTemplateText();
    }

    public MethodCodeMetadata generateMapperMethod(String methodName, boolean generated, List<MapperArgumentMethodModel> mapperMethodArguments,
        ClassMetaModel targetMetaModel, MapperCodeMetadata mapperGeneratedCodeMetadata,
        MapperConfiguration mapperConfiguration,
        PropertiesOverriddenMapping propertiesOverriddenMapping,
        MapperGenerateConfiguration mapperGenerateConfiguration,
        ObjectNodePath currentPath, MethodCodeMetadata parentMethodCodeMetadata) {

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .returnClassMetaModel(targetMetaModel)
            .methodReturnType(targetMetaModel.getJavaGenericTypeInfo())
            .methodArguments(mapperMethodArguments)
            .methodName(methodName)
            .generated(generated)
            .parentMethodMetadata(parentMethodCodeMetadata);

        mapperMethodArguments = elements(mapperMethodArguments)
            .map(methodArgument -> {
                if (methodArgument.getArgumentType().isOnlyRawClassModel()) {
                    return methodArgument.overrideType(createNotGenericClassMetaModel(methodArgument.getArgumentType(),
                        mapperGenerateConfiguration.getFieldMetaResolverForRawSource()));
                }
                return methodArgument;
            })
            .asList();

        WritePropertyStrategy writePropertyStrategy = instanceLoader.createInstanceOrGetBean(WriteToMapStrategy.class);
        if (targetMetaModel.hasRealClass() && !targetMetaModel.isSimpleType() && !targetMetaModel.isGenericModel()) {
            writePropertyStrategy = createWritePropertyStrategy(targetMetaModel);
            targetMetaModel = createNotGenericClassMetaModel(targetMetaModel, mapperGenerateConfiguration.getFieldMetaResolverForRawTarget());
        }

        var currentNodeOverriddenMappings = findOverriddenMappingStrategiesForCurrentNode(propertiesOverriddenMapping);

        if (targetMetaModel.isSimpleType()) {

            generateMapperMethodWhenMapToSimpleType(mapperMethodArguments, targetMetaModel,
                mapperGeneratedCodeMetadata, currentPath, currentNodeOverriddenMappings, methodBuilder);

        } else {
            methodBuilder
                .nextMappingCodeLine(wrapWithNextLineWith2Tabs(writePropertyStrategy.generateInitLine(targetMetaModel)))
                .writePropertyStrategy(writePropertyStrategy);

            var allTargetFields = Optional.of(targetMetaModel)
                .map(classMetaModel -> {
                    if (classMetaModel.isGenericModel()) {
                        return classMetaModel.fetchAllFields();
                    }
                    FieldMetaResolver fieldMetaResolver = findFieldMetaResolver(classMetaModel.getRealClass(),
                        mapperGenerateConfiguration.getFieldMetaResolverForRawTarget());
                    return fieldMetaResolver.getAllAvailableFieldsForWrite(classMetaModel);
                })
                .orElse(List.of()).stream()
                .sorted(writePropertyStrategy.getFieldSorter())
                .collect(Collectors.toUnmodifiableList());

            for (FieldMetaModel targetFieldMetaModel : allTargetFields) {

                List<ValueToAssignExpression> foundAssignExpressionsForField = new ArrayList<>();
                AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();
                String fieldName = targetFieldMetaModel.getFieldName();
                ObjectNodePath fieldNameNodePath = currentPath.nextNode(fieldName);

                var overriddenPropertyStrategiesByFieldName = findOverriddenMappingStrategies(propertiesOverriddenMapping, fieldName);

                ClassMetaModel targetFieldClassMetaModel = targetFieldMetaModel.getFieldType();

                PropertiesOverriddenMapping currentPropertiesOverriddenMapping = getPropertiesOverriddenMapping(propertiesOverriddenMapping, fieldName);

                if (givenFieldIsIgnored(propertiesOverriddenMapping, fieldName)) {
                    assignExpressionForFieldReference.set(new NullAssignExpression(targetFieldClassMetaModel));
                } else {
                    if (targetFieldClassMetaModel.isSimpleType()) {

                        if (overriddenPropertyStrategiesByFieldName.size() == 1) {
                            assignExpressionForFieldReference.set(overriddenPropertyStrategiesByFieldName.get(0));

                        } else if (overriddenPropertyStrategiesByFieldName.isEmpty()) {
                            if (!currentNodeOverriddenMappings.isEmpty()) {
                                for (ValueToAssignExpression currentNodeOverriddenMapping : currentNodeOverriddenMappings) {
                                    ValueToAssignCodeMetadata getPropertyCodeMetadata = currentNodeOverriddenMapping.generateCodeMetadata();
                                    ClassMetaModel classMetaModelOfMappingExpression = getPropertyCodeMetadata.getReturnClassModel();
                                    if (classMetaModelOfMappingExpression.isSimpleType()) {
                                        mapperGeneratedCodeMetadata.addError(createMessagePlaceholder("cannot.get.field.from.simple.field",
                                            fieldName, classMetaModelOfMappingExpression.getCanonicalNameOfRealClass(), currentPath));
                                    } else {
                                        FieldMetaModel foundField = classMetaModelOfMappingExpression.getFieldByName(fieldName);
                                        if (foundField != null) {
                                            foundAssignExpressionsForField.add(
                                                new FieldsChainToAssignExpression(classMetaModelOfMappingExpression,
                                                    getPropertyCodeMetadata.getValueGettingCode(),
                                                    List.of(foundField)
                                                )
                                            );
                                        }
                                    }
                                }
                            } else {

                                for (MapperArgumentMethodModel mapperMethodArgument : mapperMethodArguments) {
                                    var sourceMetaModel = mapperMethodArgument.getArgumentType();
                                    FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
                                    if (fieldFromSource != null) {
                                        foundAssignExpressionsForField.add(new FieldsChainToAssignExpression(
                                            sourceMetaModel, mapperMethodArgument.getArgumentName(), List.of(fieldFromSource)));
                                    }
                                }
                            }
                        }

                        if (assignExpressionForFieldReference.get() == null) {
                            if (foundAssignExpressionsForField.size() > 1) {
                                mapperGeneratedCodeMetadata.addError(
                                    createMessagePlaceholder("mapper.found.to.many.mappings.for.simple.type",
                                        fieldNameNodePath.getFullPath())
                                );
                            } else if (foundAssignExpressionsForField.size() == 1) {
                                assignExpressionForFieldReference.set(foundAssignExpressionsForField.get(0));
                            }
                        }

                    } else if (targetFieldClassMetaModel.hasRealClass() && isHavingElementsType(targetFieldClassMetaModel.getRealClass())) {

                        // TODO #1 next etc
                        // TODO #1 how to map collections ??? how to map generic type of them for example from Person to PersonDto or Person to Person as metamodel

                        if (targetFieldClassMetaModel.isListType()) {
                            throw new UnsupportedOperationException("not yet mapping inner list");
                        } else if (targetFieldClassMetaModel.isSetType()) {
                            throw new UnsupportedOperationException("not yet mapping inner set");
                        } else if (targetFieldClassMetaModel.isMapType()) {
                            throw new UnsupportedOperationException("not yet mapping inner maps");
                        } else if (targetFieldClassMetaModel.isArrayType()) {
                            throw new UnsupportedOperationException("not yet mapping inner array");
                        } else {
                            throw new UnsupportedOperationException("not supported write to class"
                                + targetFieldClassMetaModel.getRealClass() + " during object mapping");
                        }
                    } else {
                        List<ValueToAssignExpression> methodArgumentsExpressions = new ArrayList<>();
                        if (isNotEmpty(overriddenPropertyStrategiesByFieldName)) {
                            methodArgumentsExpressions.addAll(overriddenPropertyStrategiesByFieldName);
                        } else {

                            for (MapperArgumentMethodModel mapperMethodArgument : mapperMethodArguments) {
                                var sourceMetaModel = mapperMethodArgument.getArgumentType();
                                FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
                                if (fieldFromSource != null) {
                                    methodArgumentsExpressions.add(new FieldsChainToAssignExpression(
                                        sourceMetaModel, mapperMethodArgument.getArgumentName(), List.of(fieldFromSource)));
                                }
                            }
                        }

                        List<MapperArgumentMethodModel> nextMapperMethodArguments = elements(methodArgumentsExpressions)
                            .mapWithIndex((index, expression) -> new MapperArgumentMethodModel(
                                methodArgumentsExpressions.size() == 1 ? "sourceObject" : "argument" + index,
                                expression.generateCodeMetadata().getReturnClassModel()))
                            .asList();

                        boolean wasError = false;

                        if (methodArgumentsExpressions.size() == 1) {

                            var methodArgumentCodeMetaData = methodArgumentsExpressions.get(0).generateCodeMetadata();
                            ClassMetaModel sourceClassMetaModel = methodArgumentCodeMetaData.getReturnClassModel();

                            List<MethodCodeMetadata> foundMatchedInnerNotGeneratedMethods = mapperGeneratedCodeMetadata
                                .findMatchNotGeneratedMethod(targetFieldClassMetaModel, sourceClassMetaModel);

                            if (targetFieldClassMetaModel.isTheSameMetaModel(sourceClassMetaModel)) {
                                assignExpressionForFieldReference.set(methodArgumentsExpressions.get(0));
                            } else if (foundMatchedInnerNotGeneratedMethods.size() == 1) {
                                assignExpressionForFieldReference.set(new MethodInCurrentClassAssignExpression(
                                    foundMatchedInnerNotGeneratedMethods.get(0).getMethodName(),
                                    methodArgumentsExpressions,
                                    targetFieldClassMetaModel));
                            } else if (foundMatchedInnerNotGeneratedMethods.size() > 1) {
                                wasError = true;
                                mapperGeneratedCodeMetadata.addError(
                                    createMessagePlaceholder("mapper.found.to.many.methods",
                                        elements(foundMatchedInnerNotGeneratedMethods)
                                            .map(MethodCodeMetadata::getMethodName)
                                            .asConcatText(", "),
                                        fieldNameNodePath.getFullPath())
                                );
                            } else if (canConvertByConversionService(methodArgumentCodeMetaData.getReturnClassModel(), targetFieldClassMetaModel)) {
                                assignExpressionForFieldReference.set(methodArgumentsExpressions.get(0));
                            }
                        }

                        boolean canGenerateNestedMethod = (containsNestedMappings(currentPropertiesOverriddenMapping) || isNotEmpty(methodArgumentsExpressions));
                        if (assignExpressionForFieldReference.get() == null && !wasError && canGenerateNestedMethod) {

                            MethodCodeMetadata generatedNewMethodMeta = createMethodCodeMetadata(mapperGeneratedCodeMetadata, mapperConfiguration,
                                currentPath, fieldName, targetFieldClassMetaModel, nextMapperMethodArguments,
                                currentPropertiesOverriddenMapping, parentMethodCodeMetadata, mapperGenerateConfiguration);

                            assignExpressionForFieldReference.set(new MethodInCurrentClassAssignExpression(
                                generatedNewMethodMeta.getMethodName(),
                                methodArgumentsExpressions,
                                targetFieldClassMetaModel));
                        }
                    }
                }

                if (assignExpressionForFieldReference.get() == null) {
                    if (mapperGenerateConfiguration.isGlobalIgnoreMappingProblems()
                        || mapperConfiguration.isIgnoreMappingProblems()
                        || currentPropertiesOverriddenMapping.isIgnoreMappingProblem()) {
                        assignExpressionForFieldReference.set(new NullAssignExpression(targetFieldClassMetaModel));
                    } else {
                        String inMethodPartMessage = generated ? "" : translatePlaceholder("mapper.not.found.assign.for.method", methodName);

                        mapperGeneratedCodeMetadata.addError(createMessagePlaceholder("mapper.not.found.assign.strategy",
                            fieldName, targetMetaModel.getTypeDescription(), fieldNameNodePath.getFullPath(), inMethodPartMessage));
                    }
                }

                if (assignExpressionForFieldReference.get() != null) {
                    ValueToAssignCodeMetadata valueToAssignCodeMetadata = assignExpressionForFieldReference.get().generateCodeMetadata();
                    mapperGeneratedCodeMetadata.fetchMetaDataFrom(valueToAssignCodeMetadata);

                    String fetchValueFullExpression = generateFetchValueForAssign(valueToAssignCodeMetadata.getReturnClassModel(),
                        targetFieldClassMetaModel, valueToAssignCodeMetadata.getFullValueExpression(),
                        mapperGeneratedCodeMetadata, fieldNameNodePath);

                    String nextLine = writePropertyStrategy.generateWritePropertyCode(fieldName, fetchValueFullExpression);
                    methodBuilder.nextMappingCodeLine(wrapWithNextLineWith2Tabs(nextLine));
                }
            }
            methodBuilder.lastLine(writePropertyStrategy.generateLastLine(targetMetaModel.getJavaGenericTypeInfo()));
        }

        return methodBuilder.build();
    }

    private MethodCodeMetadata createMethodCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata,
        MapperConfiguration mapperConfiguration, ObjectNodePath currentPath,
        String fieldName, ClassMetaModel targetFieldClassMetaModel,
        List<MapperArgumentMethodModel> nextMapperMethodArguments,
        PropertiesOverriddenMapping nextMapperConfigurationNode,
        MethodCodeMetadata parentMethodCodeMetadata,
        MapperGenerateConfiguration mapperGenerateConfiguration) {

        String methodName = createMethodName(nextMapperMethodArguments, targetFieldClassMetaModel);

        MethodCodeMetadata generatedNewMethodMeta = generateMapperMethod(methodName, true,
            nextMapperMethodArguments,
            targetFieldClassMetaModel,
            mapperGeneratedCodeMetadata,
            mapperConfiguration,
            nextMapperConfigurationNode,
            mapperGenerateConfiguration,
            currentPath.nextNode(fieldName),
            parentMethodCodeMetadata);

        if (parentMethodCodeMetadata != null) {
            parentMethodCodeMetadata.addChildMethod(generatedNewMethodMeta);
        }

        MethodCodeMetadata foundMethodByTheSameCode = mapperGeneratedCodeMetadata.getMethodWhenExistsWithTheSameCode(generatedNewMethodMeta);
        boolean shouldAddAsNewMethod = false;

        if (foundMethodByTheSameCode != null) {
            if (foundMethodByTheSameCode.hasTheSameChildMethods(generatedNewMethodMeta)) {
                generatedNewMethodMeta = foundMethodByTheSameCode;
            } else {
                shouldAddAsNewMethod = true;
            }
        } else {
            shouldAddAsNewMethod = true;
        }

        if (shouldAddAsNewMethod) {
            generatedNewMethodMeta.setMethodName(regenerateMethodName(generatedNewMethodMeta.getMethodName(), mapperGeneratedCodeMetadata.getMethodNames()));

            mapperGeneratedCodeMetadata.addOtherMethod(generatedNewMethodMeta);
        }

        return generatedNewMethodMeta;
    }

    private void generateMapperMethodWhenMapToSimpleType(List<MapperArgumentMethodModel> methodArguments,
        ClassMetaModel targetMetaModel, MapperCodeMetadata mapperGeneratedCodeMetadata,
        ObjectNodePath currentPath, List<ValueToAssignExpression> currentNodeOverriddenMappings,
        MethodCodeMetadataBuilder methodBuilder) {

        String expressionForAssign = null;
        if (currentNodeOverriddenMappings.size() == 1) {
            ValueToAssignExpression propertyValueMappingStrategy = currentNodeOverriddenMappings.get(0);
            ValueToAssignCodeMetadata getPropertyCodeMetadata = propertyValueMappingStrategy.generateCodeMetadata();

            expressionForAssign = generateFetchValueForAssign(getPropertyCodeMetadata.getReturnClassModel(),
                targetMetaModel, getPropertyCodeMetadata.getFullValueExpression(), mapperGeneratedCodeMetadata, currentPath);

            mapperGeneratedCodeMetadata.fetchMetaDataFrom(getPropertyCodeMetadata);

        } else if (currentNodeOverriddenMappings.isEmpty()) {
            if (methodArguments.size() == 1) {
                expressionForAssign = generateFetchValueForAssign(methodArguments.get(0).getArgumentType(), targetMetaModel,
                    "sourceObject", mapperGeneratedCodeMetadata, currentPath);
            } else {
                mapperGeneratedCodeMetadata.addError(
                    createMessagePlaceholder(
                        "mapper.found.to.many.sources.for.simple.type", currentPath.getFullPath())
                );
            }

        } else {
            mapperGeneratedCodeMetadata.addError(
                createMessagePlaceholder(
                    "mapper.found.to.many.mappings.for.simple.type", currentPath.getFullPath())
            );
        }

        if (expressionForAssign != null) {
            methodBuilder.lastLine(wrapValueWithReturnStatement(targetMetaModel.getJavaGenericTypeInfo(), expressionForAssign));
        }
    }

    private String generateFetchValueForAssign(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel,
        String fetchValueExpression, MapperCodeMetadata mapperGeneratedCodeMetadata, ObjectNodePath currentPath) {

        if (sourceMetaModel.getTypeDescription().equals(targetMetaModel.getTypeDescription()) || sourceMetaModel.isSubTypeOf(targetMetaModel)) {
            return fetchValueExpression;
        } else {
            var converterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel);
            if (converterDefinition != null) {
                mapperGeneratedCodeMetadata.addConstructorArgument(GenericObjectsConversionService.class);
                String converterName = converterDefinition.getBeanName();
                return String.format("((%s) genericObjectsConversionService.convert(\"%s\", %s))",
                    targetMetaModel.getJavaGenericTypeInfo(), converterName, fetchValueExpression);
            } else if (sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
                && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass())) {
                mapperGeneratedCodeMetadata.addConstructorArgument(ConversionService.class);
                return String.format("conversionService.convert(%s, %s.class)",
                    fetchValueExpression, targetMetaModel.getCanonicalNameOfRealClass());
            } else {
                mapperGeneratedCodeMetadata.addError(createMessagePlaceholder(
                    "mapper.converter.not.found.between.metamodels",
                    sourceMetaModel.getTypeDescription(),
                    targetMetaModel.getTypeDescription(),
                    currentPath.getFullPath()));
                return StringUtils.EMPTY;
            }
        }
    }

    private boolean canConvertByConversionService(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        var converterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel);
        return converterDefinition != null || (sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
            && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass()));
    }
}

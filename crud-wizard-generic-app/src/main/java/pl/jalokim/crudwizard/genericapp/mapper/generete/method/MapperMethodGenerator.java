package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith2Tabs;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MappingException;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperMethodGenerator {

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

    public MethodCodeMetadata generateMapperMethod(MapperMethodGeneratorArgument methodGeneratorArgument) {

        String methodName = methodGeneratorArgument.getMethodName();
        List<MapperArgumentMethodModel> originalMapperMethodArguments = methodGeneratorArgument.getMapperMethodArguments();
        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        PropertiesOverriddenMapping propertiesOverriddenMapping = methodGeneratorArgument.getPropertiesOverriddenMapping();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();
        MethodCodeMetadata parentMethodCodeMetadata = methodGeneratorArgument.getParentMethodCodeMetadata();

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .returnClassMetaModel(targetMetaModel)
            .methodReturnType(targetMetaModel.getJavaGenericTypeInfo())
            .methodArguments(originalMapperMethodArguments)
            .methodName(methodName)
            .generated(methodGeneratorArgument.isGenerated())
            .parentMethodMetadata(parentMethodCodeMetadata);

        List<MapperArgumentMethodModel> mapperMethodArguments = elements(originalMapperMethodArguments)
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
            generateMapperMethodLinesForNotSimpleType(methodGeneratorArgument.toBuilder()
                    .targetMetaModel(targetMetaModel)
                    .mapperMethodArguments(mapperMethodArguments)
                    .build(),
                methodBuilder,
                writePropertyStrategy,
                currentNodeOverriddenMappings);
        }

        return methodBuilder.build();
    }

    private void generateMapperMethodLinesForNotSimpleType(MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, WritePropertyStrategy writePropertyStrategy,
        List<ValueToAssignExpression> currentNodeOverriddenMappings) {

        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        PropertiesOverriddenMapping propertiesOverriddenMapping = methodGeneratorArgument.getPropertiesOverriddenMapping();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        methodBuilder
            .nextMappingCodeLine(wrapWithNextLineWith2Tabs(writePropertyStrategy.generateInitLine(targetMetaModel)))
            .writePropertyStrategy(writePropertyStrategy);

        var allTargetFields = extractAllTargetFields(writePropertyStrategy, targetMetaModel, mapperGenerateConfiguration);

        for (FieldMetaModel targetFieldMetaModel : allTargetFields) {

            String fieldName = targetFieldMetaModel.getFieldName();
            ClassMetaModel classMetaModel = targetFieldMetaModel.getFieldType();

            TargetFieldMetaData targetFieldMetaData = TargetFieldMetaData.builder()
                .fieldName(fieldName)
                .fieldNameNodePath(currentPath.nextNode(fieldName))
                .targetFieldClassMetaModel(classMetaModel)
                .overriddenPropertyStrategiesByForField(findOverriddenMappingStrategies(propertiesOverriddenMapping, fieldName))
                .propertiesOverriddenMappingForField(getPropertiesOverriddenMapping(propertiesOverriddenMapping, fieldName))
                .build();

            tryFindMappingStrategyForField(methodGeneratorArgument,
                methodBuilder, writePropertyStrategy,
                currentNodeOverriddenMappings, targetFieldMetaData);
        }
        methodBuilder.lastLine(writePropertyStrategy.generateLastLine(targetMetaModel.getJavaGenericTypeInfo()));
    }

    private void tryFindMappingStrategyForField(MapperMethodGeneratorArgument methodGeneratorArgument, MethodCodeMetadataBuilder methodBuilder,
        WritePropertyStrategy writePropertyStrategy, List<ValueToAssignExpression> currentNodeOverriddenMappings,
        TargetFieldMetaData targetFieldMetaData) {

        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();

        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        String fieldName = targetFieldMetaData.getFieldName();

        String mappingProblemReason = null;
        try {
            if (givenFieldIsIgnored(methodGeneratorArgument.getPropertiesOverriddenMapping(), fieldName)) {
                assignExpressionForFieldReference.set(new NullAssignExpression(targetFieldClassMetaModel));
            } else {
                if (targetFieldClassMetaModel.isSimpleType()) {

                    assignValueForSimpleField(methodGeneratorArgument,
                        currentNodeOverriddenMappings,
                        assignExpressionForFieldReference,
                        targetFieldMetaData);

                } else if (targetFieldClassMetaModel.hasRealClass() && isHavingElementsType(targetFieldClassMetaModel.getRealClass())) {
                    assignValueToFieldWithElements(targetFieldMetaData);
                } else {
                    assignValueForObject(methodGeneratorArgument,
                        targetFieldMetaData,
                        assignExpressionForFieldReference);
                }
            }
        } catch (MappingException ex) {
            log.warn("mapping problem reason: {}", ex.getMessage());
            mappingProblemReason = ex.getMessage();
        } catch (Exception ex) {
            log.error("found unexpected mapping problem, reason: ", ex);
            mappingProblemReason = ex.getMessage();
        }

        checkThatFoundMappingStrategyForField(methodGeneratorArgument,
            methodBuilder,
            writePropertyStrategy,
            assignExpressionForFieldReference,
            targetFieldMetaData,
            mappingProblemReason);
    }

    private List<FieldMetaModel> extractAllTargetFields(WritePropertyStrategy writePropertyStrategy, ClassMetaModel targetMetaModel,
        MapperGenerateConfiguration mapperGenerateConfiguration) {
        return Optional.of(targetMetaModel)
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
    }

    private void assignValueForSimpleField(MapperMethodGeneratorArgument methodGeneratorArgument,
        List<ValueToAssignExpression> currentNodeOverriddenMappings,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData) {

        String fieldName = targetFieldMetaData.getFieldName();
        List<ValueToAssignExpression> overriddenPropertyStrategiesByFieldName = targetFieldMetaData.getOverriddenPropertyStrategiesByForField();

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        List<ValueToAssignExpression> foundAssignExpressionsForField = new ArrayList<>();

        if (overriddenPropertyStrategiesByFieldName.size() == 1) {
            assignExpressionForFieldReference.set(overriddenPropertyStrategiesByFieldName.get(0));

        } else if (overriddenPropertyStrategiesByFieldName.isEmpty()) {
            if (!currentNodeOverriddenMappings.isEmpty()) {
                for (ValueToAssignExpression currentNodeOverriddenMapping : currentNodeOverriddenMappings) {
                    ValueToAssignCodeMetadata getPropertyCodeMetadata = currentNodeOverriddenMapping.generateCodeMetadata();
                    ClassMetaModel classMetaModelOfMappingExpression = getPropertyCodeMetadata.getReturnClassModel();
                    if (classMetaModelOfMappingExpression.isSimpleType()) {
                        mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder("cannot.get.field.from.simple.field",
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

                for (MapperArgumentMethodModel mapperMethodArgument : methodGeneratorArgument.getMapperMethodArguments()) {
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
                mapperGeneratedCodeMetadata.throwMappingError(
                    createMessagePlaceholder("mapper.found.to.many.mappings.for.simple.type",
                        currentPath.nextNode(fieldName).getFullPath())
                );
            } else if (foundAssignExpressionsForField.size() == 1) {
                assignExpressionForFieldReference.set(foundAssignExpressionsForField.get(0));
            }
        }
    }

    private void assignValueToFieldWithElements(TargetFieldMetaData targetFieldMetaData) {
        // TODO #1 next etc
        // TODO #1 how to map collections ??? how to map generic type of them for example from Person to PersonDto or Person to Person as metamodel

        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();

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
    }

    private void assignValueForObject(final MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference) {

        String fieldName = targetFieldMetaData.getFieldName();
        final ObjectNodePath fieldNameNodePath = targetFieldMetaData.getFieldNameNodePath();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();

        List<ValueToAssignExpression> overriddenPropertyStrategiesByFieldName = targetFieldMetaData.getOverriddenPropertyStrategiesByForField();
        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        PropertiesOverriddenMapping propertiesOverriddenMappingForField = targetFieldMetaData.getPropertiesOverriddenMappingForField();

        List<ValueToAssignExpression> methodArgumentsExpressions = new ArrayList<>();
        if (isNotEmpty(overriddenPropertyStrategiesByFieldName)) {
            methodArgumentsExpressions.addAll(overriddenPropertyStrategiesByFieldName);
        } else {

            for (MapperArgumentMethodModel mapperMethodArgument : methodGeneratorArgument.getMapperMethodArguments()) {
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
                mapperGeneratedCodeMetadata.throwMappingError(
                    createMessagePlaceholder("mapper.found.to.many.methods",
                        elements(foundMatchedInnerNotGeneratedMethods)
                            .map(MethodCodeMetadata::getMethodName)
                            .asConcatText(", "),
                        fieldNameNodePath.getFullPath())
                );
                return;
            } else if (canConvertByConversionService(methodArgumentCodeMetaData.getReturnClassModel(), targetFieldClassMetaModel)) {
                assignExpressionForFieldReference.set(methodArgumentsExpressions.get(0));
            }
        }

        boolean canGenerateNestedMethod = (containsNestedMappings(propertiesOverriddenMappingForField) || isNotEmpty(methodArgumentsExpressions));
        if (assignExpressionForFieldReference.get() == null && canGenerateNestedMethod) {

            MethodCodeMetadata generatedNewMethodMeta = createMethodCodeMetadata(
                methodGeneratorArgument.createForNextMethod(nextMapperMethodArguments, targetFieldMetaData));

            assignExpressionForFieldReference.set(new MethodInCurrentClassAssignExpression(
                generatedNewMethodMeta.getMethodName(),
                methodArgumentsExpressions,
                targetFieldClassMetaModel));
        }
    }


    private void checkThatFoundMappingStrategyForField(MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder,
        WritePropertyStrategy writePropertyStrategy,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData,
        String mappingProblemReason) {

        String methodName = methodGeneratorArgument.getMethodName();
        boolean generated = methodGeneratorArgument.isGenerated();
        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MapperConfiguration mapperConfiguration = methodGeneratorArgument.getMapperConfiguration();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();

        String fieldName = targetFieldMetaData.getFieldName();
        ObjectNodePath fieldNameNodePath = targetFieldMetaData.getFieldNameNodePath();
        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();

        if (assignExpressionForFieldReference.get() == null) {
            if (mapperGenerateConfiguration.isGlobalIgnoreMappingProblems()
                || mapperConfiguration.isIgnoreMappingProblems()
                || targetFieldMetaData.getPropertiesOverriddenMappingForField().isIgnoreMappingProblem()) {
                assignExpressionForFieldReference.set(new NullAssignExpression(targetFieldClassMetaModel));
            } else {
                String inMethodPartMessage = generated ? "" : translatePlaceholder("mapper.not.found.assign.for.method", methodName);
                String reasonPart = mappingProblemReason == null ? "" : translatePlaceholder("mapper.mapping.problem.reason", mappingProblemReason);

                mapperGeneratedCodeMetadata.addError(createMessagePlaceholder("mapper.not.found.assign.strategy",
                    fieldName, targetMetaModel.getTypeDescription(), fieldNameNodePath.getFullPath(), inMethodPartMessage + reasonPart));
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

    private MethodCodeMetadata createMethodCodeMetadata(MapperMethodGeneratorArgument mapperMethodGeneratorArgument) {
        MapperCodeMetadata mapperGeneratedCodeMetadata = mapperMethodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MethodCodeMetadata parentMethodCodeMetadata = mapperMethodGeneratorArgument.getParentMethodCodeMetadata();

        MethodCodeMetadata generatedNewMethodMeta = generateMapperMethod(mapperMethodGeneratorArgument);

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
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder(
                    "mapper.found.to.many.sources.for.simple.type", currentPath.getFullPath()));
            }
        } else {
            mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder(
                    "mapper.found.to.many.mappings.for.simple.type", currentPath.getFullPath()));
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
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder(
                    "mapper.converter.not.found.between.metamodels",
                    sourceMetaModel.getTypeDescription(),
                    targetMetaModel.getTypeDescription(),
                    currentPath.getFullPath()));
                return null;
            }
        }
    }

    private boolean canConvertByConversionService(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        var converterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel);
        return converterDefinition != null || (sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
            && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass()));
    }
}

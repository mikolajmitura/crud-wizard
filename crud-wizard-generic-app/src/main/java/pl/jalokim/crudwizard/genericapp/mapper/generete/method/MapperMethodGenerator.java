package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith2Tabs;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.regenerateMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.containsNestedMappings;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.findOverriddenMappingStrategies;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.getPropertiesOverriddenMapping;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.givenFieldIsIgnored;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements.IterableTemplateForMappingResolver.findIterableTemplateForMappingFor;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategyFactory.createWritePropertyStrategy;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.tabsNTimes;
import static pl.jalokim.utils.template.TemplateAsText.fromText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements.IterableTemplateForMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WriteToMapStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.utils.template.TemplateAsText;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperMethodGenerator {

    public static final String ITERABLE_ELEMENT_NODE_NAME = "*";

    // TODO #1 split this class for few smaller

    // TODO #1 mapper task orders
    //  - implement mapping enums and test that
    //  - validation of correctness of mapping during add new endpoint with mappers, and test (not IT)
    //  - generate mapper code and compile it, put to classloader and test that is exists (generate few times and check that latest version was used)
    //  - generate few mappers code and compile it, put to classloader and map some values by them
    //  - implement use mapper with have input field other than GenericMapperArgument but raw object
    //  - full IT for create new endpoint with mapper with some overridden fields
    //      - invoke that endpoint and verify that mapping was correct

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final ConversionService conversionService;
    private final InstanceLoader instanceLoader;

    public MethodCodeMetadata generateMapperMethod(MapperMethodGeneratorArgument methodGeneratorArgument) {

        String methodName = methodGeneratorArgument.getMethodName();
        List<MapperArgumentMethodModel> originalMapperMethodArguments = methodGeneratorArgument.getMapperMethodArguments();
        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
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

        if (targetMetaModel.isSimpleType()) {

            generateMapperMethodWhenMapToSimpleType(mapperMethodArguments, targetMetaModel,
                mapperGeneratedCodeMetadata, currentPath, methodGeneratorArgument, methodBuilder);

        } else {
            generateMapperMethodLinesForNotSimpleType(methodGeneratorArgument.toBuilder()
                    .targetMetaModel(targetMetaModel)
                    .mapperMethodArguments(mapperMethodArguments)
                    .build(),
                methodBuilder,
                writePropertyStrategy);
        }

        return methodBuilder.build();
    }

    private void generateMapperMethodLinesForNotSimpleType(MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, WritePropertyStrategy writePropertyStrategy) {

        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        PropertiesOverriddenMapping propertiesOverriddenMapping = methodGeneratorArgument.getPropertiesOverriddenMapping();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        if (tryAssignToObjectWhenExistsOneCurrentNodeMapping(methodGeneratorArgument, methodBuilder,
            targetMetaModel, propertiesOverriddenMapping, currentPath)) {
            return;
        }

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

            List<ValueToAssignExpression> overriddenExpressionsOrFoundByFieldName = getOverriddenExpressionsOrFindByFieldName(methodGeneratorArgument,
                targetFieldMetaData);

            String expressionForAssign = tryGetMappingAssignExpression(methodGeneratorArgument,
                targetFieldMetaData, overriddenExpressionsOrFoundByFieldName);

            if (expressionForAssign != null) {
                String nextLine = writePropertyStrategy.generateWritePropertyCode(fieldName, expressionForAssign);
                methodBuilder.nextMappingCodeLine(wrapWithNextLineWith2Tabs(nextLine));
            }
        }
        methodBuilder.lastLine(writePropertyStrategy.generateLastLine(targetMetaModel.getJavaGenericTypeInfo()));
    }

    private boolean tryAssignToObjectWhenExistsOneCurrentNodeMapping(MapperMethodGeneratorArgument methodGeneratorArgument, MethodCodeMetadataBuilder methodBuilder, ClassMetaModel targetMetaModel,
        PropertiesOverriddenMapping propertiesOverriddenMapping, ObjectNodePath currentPath) {
        TargetFieldMetaData returnMethodMetaData = TargetFieldMetaData.builder()
            .fieldName("")
            .fieldNameNodePath(currentPath)
            .targetFieldClassMetaModel(targetMetaModel)
            .overriddenPropertyStrategiesByForField(methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode())
            .propertiesOverriddenMappingForField(propertiesOverriddenMapping)
            .build();

        List<ValueToAssignExpression> overriddenMappingStrategiesForCurrentNode = methodGeneratorArgument
            .findOverriddenMappingStrategiesForCurrentNode();

        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();

        if (overriddenMappingStrategiesForCurrentNode.size() == 1) {
            assignExpressionForFieldReference.set(overriddenMappingStrategiesForCurrentNode.get(0));
            String expressionForReturnLine = getExpressionForAssignWhenExists(methodGeneratorArgument,
                assignExpressionForFieldReference,
                returnMethodMetaData,
                null);

            if (expressionForReturnLine != null) {
                methodBuilder.lastLine("return " + expressionForReturnLine);
                return true;
            }
        }
        return false;
    }

    private String tryGetMappingAssignExpression(MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData,
        List<ValueToAssignExpression> methodArgumentsForMappingNotSimpleTypes) {

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
                        assignExpressionForFieldReference,
                        targetFieldMetaData);

                } else {
                    if (isElementsType(targetFieldClassMetaModel)) {
                        assignValueToFieldWithElements(methodGeneratorArgument,
                            targetFieldMetaData,
                            assignExpressionForFieldReference,
                            methodArgumentsForMappingNotSimpleTypes);

                    } else {
                        assignValueForObject(methodGeneratorArgument,
                            targetFieldMetaData,
                            methodArgumentsForMappingNotSimpleTypes,
                            assignExpressionForFieldReference);
                    }
                }
            }
        } catch (MappingException ex) {
            log.warn("mapping problem reason: {}", ex.getMessage());
            mappingProblemReason = ex.getMessage();
        } catch (Exception ex) {
            log.error("found unexpected mapping problem, reason: ", ex);
            mappingProblemReason = ex.getMessage();
        }

        return getExpressionForAssignWhenExists(methodGeneratorArgument,
            assignExpressionForFieldReference,
            targetFieldMetaData,
            mappingProblemReason);
    }

    private boolean isElementsType(ClassMetaModel targetFieldClassMetaModel) {
        return targetFieldClassMetaModel.isMapType() || targetFieldClassMetaModel.isListType()
            || targetFieldClassMetaModel.isSetType() || targetFieldClassMetaModel.isArrayType();
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
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData) {

        String fieldName = targetFieldMetaData.getFieldName();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        List<ValueToAssignExpression> foundAssignExpressionsForField = getOverriddenExpressionsOrFindByFieldName(methodGeneratorArgument,
            targetFieldMetaData);

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

    private void assignValueToFieldWithElements(MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData, AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        List<ValueToAssignExpression> methodArgumentsExpressions) {

        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();

        List<MapperArgumentMethodModel> mapperElementsMethodArguments = convertAssignExpressionsToMethodArguments(mapperGeneratedCodeMetadata,
            methodArgumentsExpressions);

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .returnClassMetaModel(targetFieldClassMetaModel)
            .methodReturnType(targetFieldClassMetaModel.getJavaGenericTypeInfo())
            .methodArguments(mapperElementsMethodArguments)
            .methodName(createMethodName(mapperElementsMethodArguments, targetFieldClassMetaModel))
            .generated(true)
            .lastLine("return elements")
            .parentMethodMetadata(methodGeneratorArgument.getParentMethodCodeMetadata());

        IterableTemplateForMapping iterableTemplateForTarget = findIterableTemplateForMappingFor(targetFieldClassMetaModel);
        Objects.requireNonNull(iterableTemplateForTarget, "iterableTemplateForTarget should not be null");

        List<String> populateElements = new ArrayList<>();
        List<ClassMetaModel> genericTypesOfTargetCollection = targetFieldClassMetaModel.getGenericTypes();

        for (MapperArgumentMethodModel methodArgument : mapperElementsMethodArguments) {
            ClassMetaModel argumentType = methodArgument.getArgumentType();
            if (iterableTemplateForTarget.canMapFromSource(argumentType)) {
                IterableTemplateForMapping iterableTemplateForSource = findIterableTemplateForMappingFor(argumentType);
                String addToElementsCode;

                AtomicBoolean foundValidExpressions = new AtomicBoolean(true);

                var populateIterableTemplate = fromText(iterableTemplateForTarget.getPopulateIterableTemplate(), true);

                if (iterableTemplateForSource == null) {
                    overrideExpressionArgumentInPopulateIterable(methodGeneratorArgument, targetFieldMetaData,
                        genericTypesOfTargetCollection.get(0), populateIterableTemplate, 0, methodArgument.getArgumentName(), methodArgument.getArgumentType());
                    addToElementsCode = tabsNTimes(2) + populateIterableTemplate.getCurrentTemplateText() + ";";

                } else {

                    elements(iterableTemplateForSource.getVariablesExpressionsForAddToIterable())
                        .forEachWithIndex((index, expression) ->
                            overrideExpressionArgumentInPopulateIterable(methodGeneratorArgument, targetFieldMetaData,
                                genericTypesOfTargetCollection.get(index), populateIterableTemplate, index, expression, methodArgument.getArgumentType()
                                    .getGenericTypes().get(index))
                        );

                    addToElementsCode = TemplateAsText.fromClassPath("templates/mapper/iterate-template", false)
                        .overrideVariable("sourceElementType",
                            iterableTemplateForSource.elementForIterateType(argumentType.getGenericTypes()))
                        .overrideVariable("sourceIterables",
                            iterableTemplateForSource.getExpressionForIterateFrom(methodArgument.getArgumentName()))
                        .overrideVariable("populateIterable", populateIterableTemplate.getCurrentTemplateText())
                        .getCurrentTemplateText();
                }
                if (foundValidExpressions.get()) {
                    populateElements.add(addToElementsCode);
                }

            } else {
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder(
                    "mapper.mapping.collection.element.problem",
                    argumentType.getTypeDescription(), targetFieldClassMetaModel.getTypeDescription()
                ));
            }
        }

        String mappingCollectionCode = TemplateAsText.fromClassPath("templates/mapper/elements-mapping-template", true)
            .overrideVariable("initSizeCalculateExpression",
                getInitSizeCalculateExpression(mapperElementsMethodArguments))
            .overrideVariable("elementsType", iterableTemplateForTarget
                .generateIterableType(genericTypesOfTargetCollection, mapperGeneratedCodeMetadata))
            .overrideVariable("initElements", iterableTemplateForTarget
                .generateNewIterable(genericTypesOfTargetCollection, mapperGeneratedCodeMetadata))
            .overrideVariable("populateElements",
                elements(populateElements).concatWithNewLines())
            .getCurrentTemplateText();

        methodBuilder.nextMappingCodeLine(mappingCollectionCode);

        MethodCodeMetadata methodForMappingElements = getGeneratedNewMethodOrGetCreatedEarlier(mapperGeneratedCodeMetadata,
            methodGeneratorArgument.getParentMethodCodeMetadata(),
            methodBuilder.build());

        assignExpressionForFieldReference.set(new MethodInCurrentClassAssignExpression(
            methodForMappingElements.getMethodName(),
            methodArgumentsExpressions,
            methodForMappingElements.getReturnClassMetaModel()
        ));
    }

    private void overrideExpressionArgumentInPopulateIterable(MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaDataWithElements,
        ClassMetaModel targetFieldClassMetaModel,
        TemplateAsText populateIterableTemplate,
        Integer index, String expression,
        ClassMetaModel sourceMetaModel) {

        List<ValueToAssignExpression> methodArgumentsForMappingNotSimpleTypes = List.of(new RawJavaCodeAssignExpression(sourceMetaModel, expression));

        TargetFieldMetaData targetFieldMetaData = TargetFieldMetaData.builder()
            .fieldName(ITERABLE_ELEMENT_NODE_NAME)
            .fieldNameNodePath(targetFieldMetaDataWithElements.getFieldNameNodePath()
                .nextNode(ITERABLE_ELEMENT_NODE_NAME))
            .targetFieldClassMetaModel(targetFieldClassMetaModel)
            .overriddenPropertyStrategiesByForField(methodArgumentsForMappingNotSimpleTypes)
            .build();

        String expressionForAssignWhenExists = tryGetMappingAssignExpression(methodGeneratorArgument,
            targetFieldMetaData, methodArgumentsForMappingNotSimpleTypes);

        if (expressionForAssignWhenExists == null) {
            populateIterableTemplate.overrideVariable("expression" + index, "null");
        } else {
            populateIterableTemplate.overrideVariable("expression" + index, expressionForAssignWhenExists);
        }
    }

    private boolean isTheSameTypeOrSubType(ClassMetaModel targetMetaModel, ClassMetaModel sourceMetaModel) {
        return sourceMetaModel.getTypeDescription().equals(targetMetaModel.getTypeDescription()) || sourceMetaModel.isSubTypeOf(targetMetaModel);
    }

    private String getInitSizeCalculateExpression(List<MapperArgumentMethodModel> mapperElementsMethodArguments) {
        return elements(mapperElementsMethodArguments)
            .map(methodArgument -> {
                ClassMetaModel argumentType = methodArgument.getArgumentType();
                String argumentName = methodArgument.getArgumentName();
                if (argumentType.isArrayType()) {
                    return argumentName + ".length";
                }
                if (argumentType.isCollectionType() || argumentType.isMapType()) {
                    return argumentName + ".size()";
                }
                return "1";
            }).asConcatText(" + ");
    }

    private void assignValueForObject(final MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData,
        List<ValueToAssignExpression> methodArgumentsExpressions,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference) {

        final ObjectNodePath fieldNameNodePath = targetFieldMetaData.getFieldNameNodePath();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();

        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        PropertiesOverriddenMapping propertiesOverriddenMappingForField = targetFieldMetaData.getPropertiesOverriddenMappingForField();

        List<MapperArgumentMethodModel> nextMapperMethodArguments = convertAssignExpressionsToMethodArguments(
            mapperGeneratedCodeMetadata, methodArgumentsExpressions);

        if (methodArgumentsExpressions.size() == 1) {

            var methodArgumentCodeMetaData = methodArgumentsExpressions.get(0).generateCodeMetadata(mapperGeneratedCodeMetadata);
            ClassMetaModel sourceClassMetaModel = methodArgumentCodeMetaData.getReturnClassModel();

            List<MethodCodeMetadata> foundMatchedInnerNotGeneratedMethods = mapperGeneratedCodeMetadata
                .findMatchNotGeneratedMethod(targetFieldClassMetaModel, sourceClassMetaModel);

            if (targetFieldClassMetaModel.isTheSameMetaModel(sourceClassMetaModel)) {
                assignExpressionForFieldReference.set(methodArgumentsExpressions.get(0));
            } else if (!containsNestedMappings(propertiesOverriddenMappingForField) && foundMatchedInnerNotGeneratedMethods.size() == 1) {
                assignExpressionForFieldReference.set(new MethodInCurrentClassAssignExpression(
                    foundMatchedInnerNotGeneratedMethods.get(0).getMethodName(),
                    methodArgumentsExpressions,
                    targetFieldClassMetaModel));
            } else if (!containsNestedMappings(propertiesOverriddenMappingForField) && foundMatchedInnerNotGeneratedMethods.size() > 1) {
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

    private List<ValueToAssignExpression> getOverriddenExpressionsOrFindByFieldName(MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData) {

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        String fieldName = targetFieldMetaData.getFieldName();
        List<ValueToAssignExpression> overriddenPropertyStrategiesByFieldName = targetFieldMetaData.getOverriddenPropertyStrategiesByForField();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        List<ValueToAssignExpression> methodArgumentsExpressions = new ArrayList<>();
        if (isNotEmpty(overriddenPropertyStrategiesByFieldName)) {
            methodArgumentsExpressions.addAll(overriddenPropertyStrategiesByFieldName);
        } else {
            methodArgumentsExpressions.addAll(findValueExpressionsInMethodArgumentsByFieldName(methodGeneratorArgument,
                mapperGeneratedCodeMetadata, fieldName, currentPath));

            if (methodArgumentsExpressions.isEmpty()) {
                methodArgumentsExpressions.addAll(findValueExpressionsByFieldName(
                    methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode(),
                    mapperGeneratedCodeMetadata, fieldName, currentPath));
            }
        }
        return methodArgumentsExpressions;
    }

    private List<ValueToAssignExpression> findValueExpressionsInMethodArgumentsByFieldName(MapperMethodGeneratorArgument methodGeneratorArgument,
        MapperCodeMetadata mapperGeneratedCodeMetadata,
        String fieldName, ObjectNodePath currentPath) {

        List<ValueToAssignExpression> foundExpressions = new ArrayList<>();
        for (MapperArgumentMethodModel mapperMethodArgument : methodGeneratorArgument.getMapperMethodArguments()) {
            var sourceMetaModel = mapperMethodArgument.getArgumentType();
            if (sourceMetaModel.isSimpleType()) {
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder("cannot.get.field.from.simple.field",
                    fieldName, sourceMetaModel.getCanonicalNameOfRealClass(), currentPath));
            } else {
                FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
                if (fieldFromSource != null) {
                    foundExpressions.add(new FieldsChainToAssignExpression(
                        sourceMetaModel, mapperMethodArgument.getArgumentName(), List.of(fieldFromSource)));
                }
            }
        }
        return foundExpressions;
    }

    private List<ValueToAssignExpression> findValueExpressionsByFieldName(List<ValueToAssignExpression> givenExpressions,
        MapperCodeMetadata mapperGeneratedCodeMetadata,
        String fieldName, ObjectNodePath currentPath) {

        List<ValueToAssignExpression> foundExpressions = new ArrayList<>();
        for (ValueToAssignExpression expression : givenExpressions) {
            ValueToAssignCodeMetadata valueToAssignCodeMetadata = expression.generateCodeMetadata(mapperGeneratedCodeMetadata);
            var sourceMetaModel = valueToAssignCodeMetadata.getReturnClassModel();
            if (sourceMetaModel.isSimpleType()) {
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder("cannot.get.field.from.simple.field",
                    fieldName, sourceMetaModel.getCanonicalNameOfRealClass(), currentPath));
            } else {
                FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
                if (fieldFromSource != null) {
                    foundExpressions.add(new FieldsChainToAssignExpression(
                        sourceMetaModel, valueToAssignCodeMetadata.getFullValueExpression(), List.of(fieldFromSource)));
                }
            }
        }
        return foundExpressions;
    }

    private List<MapperArgumentMethodModel> convertAssignExpressionsToMethodArguments(MapperCodeMetadata mapperGeneratedCodeMetadata,
        List<ValueToAssignExpression> methodArgumentsExpressions) {

        return elements(methodArgumentsExpressions)
            .mapWithIndex((index, expression) -> new MapperArgumentMethodModel(
                methodArgumentsExpressions.size() == 1 ? "sourceObject" : "argument" + index,
                expression.generateCodeMetadata(mapperGeneratedCodeMetadata).getReturnClassModel()))
            .asList();
    }

    private String getExpressionForAssignWhenExists(MapperMethodGeneratorArgument methodGeneratorArgument,
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
            ValueToAssignCodeMetadata valueToAssignCodeMetadata = assignExpressionForFieldReference.get()
                .generateCodeMetadata(mapperGeneratedCodeMetadata);

            return generateFetchValueForAssign(valueToAssignCodeMetadata.getReturnClassModel(),
                targetFieldClassMetaModel, valueToAssignCodeMetadata.getFullValueExpression(),
                mapperGeneratedCodeMetadata, fieldNameNodePath);
        }
        return null;
    }

    private MethodCodeMetadata createMethodCodeMetadata(MapperMethodGeneratorArgument mapperMethodGeneratorArgument) {
        MapperCodeMetadata mapperGeneratedCodeMetadata = mapperMethodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MethodCodeMetadata parentMethodCodeMetadata = mapperMethodGeneratorArgument.getParentMethodCodeMetadata();

        MethodCodeMetadata generatedNewMethodMeta = generateMapperMethod(mapperMethodGeneratorArgument);

        return getGeneratedNewMethodOrGetCreatedEarlier(mapperGeneratedCodeMetadata, parentMethodCodeMetadata, generatedNewMethodMeta);
    }

    private MethodCodeMetadata getGeneratedNewMethodOrGetCreatedEarlier(MapperCodeMetadata mapperGeneratedCodeMetadata,
        MethodCodeMetadata parentMethodCodeMetadata,
        MethodCodeMetadata generatedNewMethodMeta) {

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

    // TODO #1 try to use assignValueForSimpleField
    private void generateMapperMethodWhenMapToSimpleType(List<MapperArgumentMethodModel> methodArguments,
        ClassMetaModel targetMetaModel, MapperCodeMetadata mapperGeneratedCodeMetadata,
        ObjectNodePath currentPath, MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder) {

        var currentNodeOverriddenMappings = methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode();

        String expressionForAssign = null;
        if (currentNodeOverriddenMappings.size() == 1) {
            ValueToAssignExpression propertyValueMappingStrategy = currentNodeOverriddenMappings.get(0);
            ValueToAssignCodeMetadata getPropertyCodeMetadata = propertyValueMappingStrategy.generateCodeMetadata(mapperGeneratedCodeMetadata);

            expressionForAssign = generateFetchValueForAssign(getPropertyCodeMetadata.getReturnClassModel(),
                targetMetaModel, getPropertyCodeMetadata.getFullValueExpression(), mapperGeneratedCodeMetadata, currentPath);

        } else if (currentNodeOverriddenMappings.isEmpty()) {
            if (methodArguments.size() == 1) {
                expressionForAssign = generateFetchValueForAssign(methodArguments.get(0).getArgumentType(), targetMetaModel,
                    "sourceObject", mapperGeneratedCodeMetadata, currentPath);
            } else {
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder(
                    "mapper.found.to.many.mappings.for.simple.type", currentPath.getFullPath()));
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

        if (isTheSameTypeOrSubType(targetMetaModel, sourceMetaModel)) {
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

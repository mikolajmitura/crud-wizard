package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith2Tabs;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.regenerateMethodName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.containsNestedMappings;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.findOverriddenMappingStrategies;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.getPropertiesOverriddenMapping;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping.givenFieldIsIgnored;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.ExpressionSourcesUtils.convertAssignExpressionsToMethodArguments;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.ExpressionSourcesUtils.getOverriddenExpressionsOrFindByFieldName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.TargetClassMetaModelUtils.extractAllTargetFields;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.method.TargetClassMetaModelUtils.isElementsType;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements.IterableTemplateForMappingResolver.findIterableTemplateForMappingFor;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression.NULL_ASSIGN;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategyFactory.createWritePropertyStrategy;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createNotGenericClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.tabsNTimes;
import static pl.jalokim.utils.template.TemplateAsText.fromText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MappingException;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGeneratorArgument.FindMethodArgument;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements.IterableTemplateForMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WriteToMapStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.utils.template.TemplateAsText;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperMethodGenerator {

    public static final String ITERABLE_ELEMENT_NODE_NAME = "*";

    // TODO #1 mapper task orders
    //  - validation of correctness of mapping during add new endpoint with mappers, and test (not IT) #validation
    //  - validation of correctness of mapping during add new endpoint with mappers, and test (IT what is better and simpler) #validation
    //  - generate mapper code and compile it, put to classloader and test that is exists (generate few times and check that latest version was used)
    //  - generate few mappers code and compile it, put to classloader and map some values by them
    //  - implement use mapper with have input field other than GenericMapperArgument but raw object
    //  - full IT for create new endpoint with mapper with some overridden fields
    //      - invoke that endpoint and verify that mapping was correct

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final AssignExpressionAsTextResolver assignExpressionAsTextResolver;
    private final SimpleTargetAssignResolver simpleTargetAssignResolver;
    private final ConversionService conversionService;
    private final InstanceLoader instanceLoader;

    public MethodCodeMetadata generateMapperMethod(MapperMethodGeneratorArgument methodGeneratorArgument) {

        String methodName = methodGeneratorArgument.getMethodName();
        List<MapperArgumentMethodModel> originalMapperMethodArguments = methodGeneratorArgument.getMapperMethodArguments();
        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
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
                if (methodArgument.getArgumentType().isOnlyRawClassModel() && !isElementsType(methodArgument.getArgumentType())) {
                    return methodArgument.overrideType(createNotGenericClassMetaModel(methodArgument.getArgumentType(),
                        mapperGenerateConfiguration.getFieldMetaResolverForRawSource()), methodArgument.getDerivedFromExpression());
                }
                return methodArgument;
            })
            .asList();

        WritePropertyStrategy writePropertyStrategy = instanceLoader.createInstanceOrGetBean(WriteToMapStrategy.class);
        if (targetMetaModel.hasRealClass() && !targetMetaModel.isSimpleType()
            && !targetMetaModel.isGenericModel() && !targetMetaModel.isArrayOrCollection()) {
            writePropertyStrategy = createWritePropertyStrategy(targetMetaModel);
            targetMetaModel = createNotGenericClassMetaModel(targetMetaModel, mapperGenerateConfiguration.getFieldMetaResolverForRawTarget());
        }

        TargetFieldMetaData returnMethodMetaData = TargetFieldMetaData.builder()
            .fieldName("")
            .fieldNameNodePath(currentPath)
            .targetFieldClassMetaModel(targetMetaModel)
            .overriddenPropertyStrategiesByForField(methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode())
            .propertiesOverriddenMappingForField(methodGeneratorArgument.getPropertiesOverriddenMapping())
            .build();

        if (targetMetaModel.isSimpleType()) {

            simpleTargetAssignResolver.generateMapperMethodWhenMapToSimpleType(mapperMethodArguments, methodGeneratorArgument,
                methodBuilder, returnMethodMetaData);

        } else if (isElementsType(targetMetaModel)) {
            ValueToAssignCodeMetadata valueToAssignCodeMetadata = getValueToAssignCodeMetadataForElements(
                methodGeneratorArgument, mapperMethodArguments.get(0).getArgumentType(), returnMethodMetaData);

            methodBuilder.lastLine("return " + valueToAssignCodeMetadata.getValueGettingCode());

        } else {
            generateMapperMethodLinesForNotSimpleType(methodGeneratorArgument.toBuilder()
                    .targetMetaModel(targetMetaModel)
                    .mapperMethodArguments(mapperMethodArguments)
                    .parentMapperMethodGeneratorArgument(methodGeneratorArgument)
                    .build(),
                methodBuilder,
                writePropertyStrategy, returnMethodMetaData);
        }

        return methodBuilder.build();
    }

    private ValueToAssignCodeMetadata getValueToAssignCodeMetadataForElements(MapperMethodGeneratorArgument methodGeneratorArgument,
        ClassMetaModel sourceMetaModel, TargetFieldMetaData returnMethodMetaData) {

        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();
        assignValueToFieldWithElements(methodGeneratorArgument,
            returnMethodMetaData,
            assignExpressionForFieldReference,
            List.of(new RawJavaCodeAssignExpression(sourceMetaModel, "sourceObject")));

        return assignExpressionForFieldReference.get()
            .generateCodeMetadata(methodGeneratorArgument.getMapperGeneratedCodeMetadata());
    }

    private void generateMapperMethodLinesForNotSimpleType(MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, WritePropertyStrategy writePropertyStrategy, TargetFieldMetaData returnMethodMetaData) {

        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        PropertiesOverriddenMapping propertiesOverriddenMapping = methodGeneratorArgument.getPropertiesOverriddenMapping();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        if (tryAssignToObjectWhenExistsOneCurrentNodeMapping(methodGeneratorArgument, methodBuilder, returnMethodMetaData)) {
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

    private boolean tryAssignToObjectWhenExistsOneCurrentNodeMapping(MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, TargetFieldMetaData returnMethodMetaData) {

        List<ValueToAssignExpression> overriddenMappingStrategiesForCurrentNode = methodGeneratorArgument
            .findOverriddenMappingStrategiesForCurrentNode();

        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();

        if (overriddenMappingStrategiesForCurrentNode.size() == 1) {
            assignExpressionForFieldReference.set(overriddenMappingStrategiesForCurrentNode.get(0));
            String expressionForReturnLine = assignExpressionAsTextResolver.getExpressionForAssignWhenExists(methodGeneratorArgument,
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
                    simpleTargetAssignResolver.assignValueForSimpleField(methodGeneratorArgument,
                        assignExpressionForFieldReference,
                        targetFieldMetaData,
                        methodArgumentsForMappingNotSimpleTypes);

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

        return assignExpressionAsTextResolver.getExpressionForAssignWhenExists(methodGeneratorArgument,
            assignExpressionForFieldReference,
            targetFieldMetaData,
            mappingProblemReason);
    }

    public void assignValueToFieldWithElements(MapperMethodGeneratorArgument methodGeneratorArgument,
        TargetFieldMetaData targetFieldMetaData, AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        List<ValueToAssignExpression> methodArgumentsExpressions) {

        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();

        List<MapperArgumentMethodModel> mapperElementsMethodArguments = convertAssignExpressionsToMethodArguments(mapperGeneratedCodeMetadata,
            methodArgumentsExpressions);

        MapperMethodGeneratorArgument methodGeneratorArgumentForCollection = methodGeneratorArgument
            .createForNextMethod(mapperElementsMethodArguments, targetFieldMetaData);

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .returnClassMetaModel(targetFieldClassMetaModel)
            .methodReturnType(targetFieldClassMetaModel.getJavaGenericTypeInfo())
            .methodArguments(mapperElementsMethodArguments)
            .methodName(createMethodName(mapperElementsMethodArguments, targetFieldClassMetaModel))
            .generated(true)
            .lastLine("return elements")
            .parentMethodMetadata(methodGeneratorArgumentForCollection.getParentMethodCodeMetadata());

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
                    overrideExpressionArgumentInPopulateIterable(methodGeneratorArgumentForCollection, targetFieldMetaData,
                        genericTypesOfTargetCollection.get(0), populateIterableTemplate, 0, methodArgument.getArgumentName(),
                        methodArgument.getArgumentType(), methodArgument);
                    addToElementsCode = tabsNTimes(2) + populateIterableTemplate.getCurrentTemplateText() + ";";

                } else {

                    elements(iterableTemplateForSource.getVariablesExpressionsForAddToIterable())
                        .forEachWithIndex((index, expression) ->
                            overrideExpressionArgumentInPopulateIterable(methodGeneratorArgumentForCollection, targetFieldMetaData,
                                genericTypesOfTargetCollection.get(index), populateIterableTemplate, index, expression, methodArgument.getArgumentType()
                                    .getGenericTypes().get(index), methodArgument)
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
        Integer index, String sourceVariableName,
        ClassMetaModel sourceMetaModel,
        MapperArgumentMethodModel methodArgument) {

        List<ValueToAssignExpression> methodArgumentsForMappingNotSimpleTypes = List.of(new RawJavaCodeAssignExpression(sourceMetaModel, sourceVariableName));
        PropertiesOverriddenMapping propertiesOverriddenMapping = methodGeneratorArgument.getPropertiesOverriddenMapping();

        TargetFieldMetaData targetFieldMetaData = TargetFieldMetaData.builder()
            .fieldName(ITERABLE_ELEMENT_NODE_NAME)
            .fieldNameNodePath(targetFieldMetaDataWithElements.getFieldNameNodePath()
                .nextNode(ITERABLE_ELEMENT_NODE_NAME))
            .targetFieldClassMetaModel(targetFieldClassMetaModel)
            .overriddenPropertyStrategiesByForField(findOverriddenMappingStrategies(propertiesOverriddenMapping, ITERABLE_ELEMENT_NODE_NAME))
            .propertiesOverriddenMappingForField(getPropertiesOverriddenMapping(propertiesOverriddenMapping, ITERABLE_ELEMENT_NODE_NAME))
            .build();

        methodGeneratorArgument = methodGeneratorArgument.createForNextMethod(List.of(methodArgument), targetFieldMetaData);

        String expressionForAssignWhenExists = tryGetMappingAssignExpression(methodGeneratorArgument,
            targetFieldMetaData, methodArgumentsForMappingNotSimpleTypes);

        if (expressionForAssignWhenExists == null) {
            populateIterableTemplate.overrideVariable("expression" + index, NULL_ASSIGN);
        } else {
            populateIterableTemplate.overrideVariable("expression" + index, expressionForAssignWhenExists);
        }
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

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();
        MapperConfiguration mapperConfiguration = methodGeneratorArgument.getMapperConfiguration();

        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        PropertiesOverriddenMapping propertiesOverriddenMappingForField = targetFieldMetaData.getPropertiesOverriddenMappingForField();

        List<MapperArgumentMethodModel> nextMapperMethodArguments = convertAssignExpressionsToMethodArguments(
            mapperGeneratedCodeMetadata, methodArgumentsExpressions);

        if (methodArgumentsExpressions.size() == 1) {

            var methodArgumentCodeMetaData = methodArgumentsExpressions.get(0).generateCodeMetadata(mapperGeneratedCodeMetadata);
            ClassMetaModel sourceClassMetaModel = methodArgumentCodeMetaData.getReturnClassModel();

            List<MethodMetadataMapperConfig> foundMatchedInnerNotGeneratedMethods = methodGeneratorArgument
                .findMethodsFor(new FindMethodArgument(
                    methodGeneratorArgument.getMapperGeneratedCodeMetadata(),
                    targetFieldClassMetaModel,
                    sourceClassMetaModel
                ));

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
                            .map(MethodMetadataMapperConfig::getMethodName)
                            .asConcatText(", "))
                );
                return;
            } else if (isEnabledAutoMapping(mapperGenerateConfiguration, mapperConfiguration)
                && canConvertByConversionService(methodArgumentCodeMetaData.getReturnClassModel(), targetFieldClassMetaModel) ) {
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

    private MethodCodeMetadata createMethodCodeMetadata(MapperMethodGeneratorArgument mapperMethodGeneratorArgument) {
        MapperCodeMetadata mapperGeneratedCodeMetadata = mapperMethodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MethodCodeMetadata parentMethodCodeMetadata = mapperMethodGeneratorArgument.getParentMethodCodeMetadata();

        MethodCodeMetadata generatedNewMethodMeta = generateMapperMethod(mapperMethodGeneratorArgument);

        return getGeneratedNewMethodOrGetCreatedEarlier(mapperGeneratedCodeMetadata, parentMethodCodeMetadata, generatedNewMethodMeta);
    }

    static MethodCodeMetadata getGeneratedNewMethodOrGetCreatedEarlier(MapperCodeMetadata mapperGeneratedCodeMetadata,
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

    private boolean canConvertByConversionService(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        var converterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel);
        return converterDefinition != null || (sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
            && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass()));
    }

    private boolean isEnabledAutoMapping(MapperGenerateConfiguration mapperGenerateConfiguration, MapperConfiguration mapperConfiguration) {
        return mapperGenerateConfiguration.isGlobalEnableAutoMapping() && mapperConfiguration.isEnableAutoMapping();
    }
}

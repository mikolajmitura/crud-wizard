package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelDescribeHelper.getClassModelInfoForGeneratedCode;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith2Tabs;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression.NULL_EXPRESSION;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategyFactory.createWritePropertyStrategy;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isHavingElementsType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WriteToMapStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolver;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.utils.template.TemplateAsText;

@Component
@RequiredArgsConstructor
public class MapperCodeGenerator {

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final ConversionService conversionService;
    private final InstanceLoader instanceLoader;

    public String generateMapperCodeMetadata(MapperConfiguration mapperConfiguration) {
        var sourceMetaModel = mapperConfiguration.getSourceMetaModel();
        var targetMetaModel = mapperConfiguration.getTargetMetaModel();

        MapperCodeMetadata mapperGeneratedCodeMetadata = new MapperCodeMetadata();

        mapperGeneratedCodeMetadata.setMapperClassName(
            String.format("%s_To_%s_Mapper",
                getClassModelInfoForGeneratedCode(sourceMetaModel),
                getClassModelInfoForGeneratedCode(targetMetaModel)
            ));

        generateMapperMethod(sourceMetaModel, targetMetaModel,
            mapperGeneratedCodeMetadata,
            mapperGeneratedCodeMetadata::setMainMethodCodeMetadata,
            mapperConfiguration,
            ObjectNodePath.rootNode());

        return generateCode(mapperGeneratedCodeMetadata);
    }

    private String generateCode(MapperCodeMetadata mapperGeneratedCodeMetadata) {

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

    public void generateMapperMethod(ClassMetaModel sourceMetaModel,
        ClassMetaModel targetMetaModel,
        MapperCodeMetadata mapperGeneratedCodeMetadata,
        Consumer<MethodCodeMetadata> generatedMethodCodeMetadataConsumer,
        MapperConfiguration mapperConfiguration, ObjectNodePath currentPath) {

        MethodCodeMetadataBuilder methodBuilder = MethodCodeMetadata.builder()
            .methodReturnType(targetMetaModel.getJavaGenericTypeInfo())
            .currentNodeType(sourceMetaModel.getJavaGenericTypeInfo())
            .methodName(sourceMetaModel, targetMetaModel);

        if (sourceMetaModel.isOnlyRawClassModel()) {
            sourceMetaModel = ClassMetaModelFactory.resolveClassMetaModelByClass(sourceMetaModel.getRealClass(),
                mapperConfiguration.getFieldMetaResolverForRawSourceDto());
        }

        WritePropertyStrategy writePropertyStrategy = instanceLoader.createInstanceOrGetBean(WriteToMapStrategy.class);
        if (targetMetaModel.isOnlyRawClassModel() && !targetMetaModel.isSimpleType() && !targetMetaModel.isGenericModel()) {
            writePropertyStrategy = createWritePropertyStrategy(targetMetaModel);
            targetMetaModel = ClassMetaModelFactory.resolveClassMetaModelByClass(targetMetaModel.getRealClass(),
                mapperConfiguration.getFieldMetaResolverForRawTargetDto());
        }

        var currentNodeOverriddenMappings = mapperConfiguration.findOverriddenMappingStrategiesForCurrentNode();

        if (targetMetaModel.isSimpleType()) {

            generateMapperMethodWhenMapToSimpleType(sourceMetaModel, targetMetaModel,
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
                        mapperConfiguration.getFieldMetaResolverForRawTargetDto());
                    return fieldMetaResolver.getAllAvailableFieldsForWrite(classMetaModel);
                })
                .orElse(List.of()).stream()
                .sorted(writePropertyStrategy.getFieldSorter())
                .collect(Collectors.toUnmodifiableList());

            for (FieldMetaModel targetFieldMetaModel : allTargetFields) {

                List<ValueToAssignExpression> foundAssignExpressionsForField = new ArrayList<>();
                ValueToAssignExpression assignExpressionForField = null;
                String fieldName = targetFieldMetaModel.getFieldName();
                ObjectNodePath fieldNameNodePath = currentPath.nextNode(fieldName);

                var overriddenPropertyStrategies = mapperConfiguration
                    .findOverriddenMappingStrategies(fieldName);

                ClassMetaModel fieldClassMetaModel = targetFieldMetaModel.getFieldType();

                if (mapperConfiguration.givenFieldIsIgnored(fieldName)) {
                    assignExpressionForField = NULL_EXPRESSION;
                } else {
                    if (fieldClassMetaModel.isSimpleType()) {

                        if (overriddenPropertyStrategies.size() == 1) {
                            assignExpressionForField = overriddenPropertyStrategies.get(0);

                        } else if (overriddenPropertyStrategies.isEmpty()) {
                            if (!currentNodeOverriddenMappings.isEmpty()) {
                                for (ValueToAssignExpression currentNodeOverriddenMapping : currentNodeOverriddenMappings) {
                                    ValueToAssignCodeMetadata getPropertyCodeMetadata = currentNodeOverriddenMapping.generateCodeMetadata();
                                    ClassMetaModel classMetaModelOfMappingExpression = getPropertyCodeMetadata.getReturnClassModel();
                                    if (classMetaModelOfMappingExpression.isSimpleType()) {
                                        throw new TechnicalException(createMessagePlaceholder("cannot.get.field.from.simple.field",
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
                                FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
                                if (fieldFromSource != null) {
                                    foundAssignExpressionsForField.add(new FieldsChainToAssignExpression(
                                        sourceMetaModel, "sourceObject", List.of(fieldFromSource)));
                                }
                            }
                        }

                        if (assignExpressionForField == null ) {
                            if (foundAssignExpressionsForField.size() > 1) {
                                throw new TechnicalException(createMessagePlaceholder(
                                    "mapper.found.to.many.mappings.for.simple.type",
                                    fieldNameNodePath.getFullPath()));
                            } else if (foundAssignExpressionsForField.size() == 1) {
                                assignExpressionForField = foundAssignExpressionsForField.get(0);
                            }
                        }

                    } else if (isHavingElementsType(fieldClassMetaModel.getRealClass())) {

                        // TODO #1 next etc
                        // TODO #1 how to map collections ??? how to map generic type of them for example from Person to PersonDto or Person to Person as metamodel

                        if (fieldClassMetaModel.isListType()) {
                            throw new UnsupportedOperationException("not yet mapping inner list");
                        } else if (fieldClassMetaModel.isSetType()) {
                            throw new UnsupportedOperationException("not yet mapping inner set");
                        } else if (fieldClassMetaModel.isMapType()) {
                            throw new UnsupportedOperationException("not yet mapping inner maps");
                        } else if (fieldClassMetaModel.isArrayType()) {
                            throw new UnsupportedOperationException("not yet mapping inner array");
                        } else {
                            throw new UnsupportedOperationException("not supported write to class"
                                + fieldClassMetaModel.getRealClass() + " during object mapping");
                        }
                    } else {

                        throw new UnsupportedOperationException("not yet mapping inner object");

                    }

                }

                if (assignExpressionForField == null) {
                    if (mapperConfiguration.isGlobalIgnoreMappingProblems()
                        || mapperConfiguration.getPropertyOverriddenMapping().isIgnoreMappingProblem()) {
                        assignExpressionForField = NULL_EXPRESSION;
                    } else {
                        // TODO #1 instead of throwing all exceptions in this class put them (if can be) info to
                        //  context and finally throw exception with all errors...
                        throw new TechnicalException(createMessagePlaceholder("mapper.not.found.assign.strategy",
                            fieldName, targetMetaModel.getTypeDescription(), fieldNameNodePath.getFullPath()));
                    }
                }
                ValueToAssignCodeMetadata valueToAssignCodeMetadata = assignExpressionForField.generateCodeMetadata();
                mapperGeneratedCodeMetadata.fetchMetaDataFrom(valueToAssignCodeMetadata);

                String fetchValueFullExpression = generateFetchValueForAssign(valueToAssignCodeMetadata.getReturnClassModel(),
                    fieldClassMetaModel, valueToAssignCodeMetadata.getFullValueExpression(),
                    mapperGeneratedCodeMetadata, fieldNameNodePath);

                String nextLine = writePropertyStrategy.generateWritePropertyCode(fieldName, fetchValueFullExpression);
                methodBuilder.nextMappingCodeLine(wrapWithNextLineWith2Tabs(nextLine));

            }
            methodBuilder.lastLine(writePropertyStrategy.generateLastLine(targetMetaModel.getJavaGenericTypeInfo()));
        }

        generatedMethodCodeMetadataConsumer.accept(methodBuilder.build());
    }

    private void generateMapperMethodWhenMapToSimpleType(ClassMetaModel sourceMetaModel,
        ClassMetaModel targetMetaModel, MapperCodeMetadata mapperGeneratedCodeMetadata,
        ObjectNodePath currentPath, List<ValueToAssignExpression> currentNodeOverriddenMappings,
        MethodCodeMetadataBuilder methodBuilder) {

        String expressionForAssign;
        if (currentNodeOverriddenMappings.size() == 1) {
            ValueToAssignExpression propertyValueMappingStrategy = currentNodeOverriddenMappings.get(0);
            ValueToAssignCodeMetadata getPropertyCodeMetadata = propertyValueMappingStrategy.generateCodeMetadata();

            expressionForAssign = generateFetchValueForAssign(getPropertyCodeMetadata.getReturnClassModel(),
                targetMetaModel, getPropertyCodeMetadata.getFullValueExpression(), mapperGeneratedCodeMetadata, currentPath);

            mapperGeneratedCodeMetadata.fetchMetaDataFrom(getPropertyCodeMetadata);

        } else if (currentNodeOverriddenMappings.isEmpty()) {
            expressionForAssign = generateFetchValueForAssign(sourceMetaModel, targetMetaModel,
                "sourceObject", mapperGeneratedCodeMetadata, currentPath);

        } else {
            throw new TechnicalException(createMessagePlaceholder(
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
                return String.format("genericObjectsConversionService.convert(%s, %s)", converterName, fetchValueExpression);
            } else if (sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
                && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass())) {
                mapperGeneratedCodeMetadata.addConstructorArgument(ConversionService.class);
                return String.format("conversionService.convert(%s, %s.class)",
                    fetchValueExpression, targetMetaModel.getCanonicalNameOfRealClass());
            } else {
                throw new TechnicalException(createMessagePlaceholder(
                    "mapper.converter.not.found.between.metamodels",
                    sourceMetaModel.getTypeDescription(),
                    targetMetaModel.getTypeDescription(),
                    currentPath.getFullPath()));
            }
        }
    }
}

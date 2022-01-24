package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelDescribeHelper.getClassModelInfoForGeneratedCode;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapValueWithReturnStatement;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith2Tabs;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategyFactory.createWritePropertyStrategy;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverFactory.findFieldMetaResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FromFieldsChainStrategy;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.GetPropertyCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.PropertyValueMappingStrategy;
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

    private final ConversionService conversionService;
    private final InstanceLoader instanceLoader;

    public String generateMapperCodeMetadata(ClassMetaModel sourceMetaModel,
        ClassMetaModel targetMetaModel, MapperConfiguration mapperConfiguration) {

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

        if (sourceMetaModel.isOnlyRawClassModel()) {
            sourceMetaModel = ClassMetaModelFactory.resolveClassMetaModelByClass(sourceMetaModel.getRealClass(),
                mapperConfiguration.getFieldMetaResolverForRawSourceDto());
        }

        WritePropertyStrategy writePropertyStrategy = instanceLoader.createInstanceOrGetBean(WriteToMapStrategy.class);
        if (targetMetaModel.isOnlyRawClassModel() && !targetMetaModel.isSimpleType() && !targetMetaModel.isGenericClassModel()) {
            writePropertyStrategy = createWritePropertyStrategy(targetMetaModel);
            targetMetaModel = ClassMetaModelFactory.resolveClassMetaModelByClass(targetMetaModel.getRealClass(),
                mapperConfiguration.getFieldMetaResolverForRawTargetDto());
        }

        Map<String, List<PropertyValueMappingStrategy>> foundFieldStrategy = new HashMap<>();

        var currentNodeOverriddenMappings = mapperConfiguration.findOverriddenMappingStrategiesForCurrentNode();

        if (targetMetaModel.isSimpleType()) {

            generateMapperMethodWhenMapToSimpleType(sourceMetaModel, targetMetaModel, mapperGeneratedCodeMetadata,
                generatedMethodCodeMetadataConsumer, currentPath, currentNodeOverriddenMappings);

        } else {
            var methodBuilder = MethodCodeMetadata.builder()
            .nextMappingCodeLine(wrapWithNextLineWith2Tabs(writePropertyStrategy.generateInitLine(targetMetaModel)))
            .methodReturnType(targetMetaModel.getJavaGenericTypeInfo())
            .currentNodeType(sourceMetaModel.getJavaGenericTypeInfo())
            .methodName(sourceMetaModel, targetMetaModel)
            .writePropertyStrategy(writePropertyStrategy);

            var allFields = Optional.of(targetMetaModel)
                .map(classMetaModel ->  {
                    if (classMetaModel.isGenericClassModel()) {
                        return classMetaModel.fetchAllFields();
                    }
                    FieldMetaResolver fieldMetaResolver = findFieldMetaResolver(classMetaModel.getRealClass(),
                        mapperConfiguration.getFieldMetaResolverForRawTargetDto());
                    return fieldMetaResolver.getAllAvailableFieldsForWrite(classMetaModel);
                })
                .orElse(List.of()).stream()
                .sorted(writePropertyStrategy.getFieldSorter())
                .collect(Collectors.toUnmodifiableList());

            for (FieldMetaModel fieldMetaModel : allFields) {

                String fieldName = fieldMetaModel.getFieldName();
                ObjectNodePath fieldNameNodePath = currentPath.nextNode(fieldName);

                var overriddenPropertyStrategies = mapperConfiguration
                    .findOverriddenMappingStrategies(fieldName);

                if (fieldMetaModel.getFieldType().isSimpleType()) {

                    ClassMetaModel fetchExpressionClassMetaModel;
                    String fetchValueExpression;

                    if (overriddenPropertyStrategies.size() == 1) {
                        PropertyValueMappingStrategy propertyValueMappingStrategy = overriddenPropertyStrategies.get(0);
                        GetPropertyCodeMetadata getPropertyCodeMetadata = propertyValueMappingStrategy.generateReturnCodeMetadata();
                        fetchExpressionClassMetaModel = getPropertyCodeMetadata.getReturnClassModel();
                        fetchValueExpression = getPropertyCodeMetadata.getFullValueExpression();
                        mapperGeneratedCodeMetadata.fetchMetaDataFrom(getPropertyCodeMetadata);
                    } else if (overriddenPropertyStrategies.isEmpty()) {
                        FieldMetaModel fieldFromSource = sourceMetaModel.getFieldByName(fieldName);
                        if (fieldFromSource == null) {
                            throw new TechnicalException(createMessagePlaceholder("mapper.not.found.field.in.source.type",
                                fieldName, sourceMetaModel.getTypeDescription(),
                                fieldNameNodePath.getFullPath(), targetMetaModel.getTypeDescription()));
                        }
                        fetchExpressionClassMetaModel = fieldFromSource.getFieldType();
                        FromFieldsChainStrategy fromFieldsChainStrategy = new FromFieldsChainStrategy(sourceMetaModel,
                            "sourceObject", List.of(fieldFromSource));
                        GetPropertyCodeMetadata getPropertyCodeMetadata = fromFieldsChainStrategy.generateReturnCodeMetadata();
                        fetchValueExpression = getPropertyCodeMetadata.getFullValueExpression();
                        mapperGeneratedCodeMetadata.fetchMetaDataFrom(getPropertyCodeMetadata);
                    } else {
                        throw new TechnicalException(createMessagePlaceholder(
                            "mapper.found.to.many.mappings.for.simple.type",
                            fieldNameNodePath.getFullPath()));
                    }

                    String fetchValueFullExpression = generateFetchValueForAssign(fetchExpressionClassMetaModel,
                        fieldMetaModel.getFieldType(), fetchValueExpression,
                        mapperGeneratedCodeMetadata, fieldNameNodePath);

                    String nextLine = writePropertyStrategy.generateWritePropertyCode(fieldName, fetchValueFullExpression);
                    methodBuilder.nextMappingCodeLine(wrapWithNextLineWith2Tabs(nextLine));
                    methodBuilder.lastLine(writePropertyStrategy.generateLastLine(targetMetaModel.getJavaGenericTypeInfo()));
                } else {
                    throw new UnsupportedOperationException("not yet");
                    // TODO #1 next etc
                    // TODO #1 how to map collections ??? how to map generic type of them for example from Person to PersonDto or Person to Person as metamodel
                }

            }
            generatedMethodCodeMetadataConsumer.accept(methodBuilder.build());
        }
    }

    private void generateMapperMethodWhenMapToSimpleType(ClassMetaModel sourceMetaModel,
        ClassMetaModel targetMetaModel, MapperCodeMetadata mapperGeneratedCodeMetadata,
        Consumer<MethodCodeMetadata> generatedMethodCodeMetadataConsumer,
        ObjectNodePath currentPath, List<PropertyValueMappingStrategy> currentNodeOverriddenMappings) {

        if (currentNodeOverriddenMappings.size() == 1) {
            PropertyValueMappingStrategy propertyValueMappingStrategy = currentNodeOverriddenMappings.get(0);
            GetPropertyCodeMetadata getPropertyCodeMetadata = propertyValueMappingStrategy.generateReturnCodeMetadata();
            generateMethodWithOnlyReturnLine(getPropertyCodeMetadata.getReturnClassModel(), targetMetaModel,
                mapperGeneratedCodeMetadata, generatedMethodCodeMetadataConsumer,
                currentPath, getPropertyCodeMetadata.getFullValueExpression());
            mapperGeneratedCodeMetadata.fetchMetaDataFrom(getPropertyCodeMetadata);

        } else if (currentNodeOverriddenMappings.isEmpty()) {
            generateMethodWithOnlyReturnLine(sourceMetaModel, targetMetaModel,
                mapperGeneratedCodeMetadata, generatedMethodCodeMetadataConsumer,
                currentPath, "sourceObject");
        } else {
            throw new TechnicalException(createMessagePlaceholder(
                "mapper.found.to.many.mappings.for.simple.type", currentPath.getFullPath()));
        }
    }

    private String generateFetchValueForAssign(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel,
        String fetchValueExpression, MapperCodeMetadata mapperGeneratedCodeMetadata, ObjectNodePath currentPath) {
        if (sourceMetaModel.isOnlyRawClassModel()) {
            if (sourceMetaModel.getRealClass().equals(targetMetaModel.getRealClass())) {
                return fetchValueExpression;
            } else if (conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass())) {
                mapperGeneratedCodeMetadata.addConstructorArgument(ConversionService.class);
                return String.format("conversionService.convert(%s, %s.class)",
                    fetchValueExpression, targetMetaModel.getCanonicalNameOfRealClass());
            } else {
                throw new TechnicalException(createMessagePlaceholder(
                    "mapper.converter.not.found.between.classes",
                    sourceMetaModel.getCanonicalNameOfRealClass(),
                    targetMetaModel.getCanonicalNameOfRealClass(),
                    currentPath.getFullPath()));
            }
        } else {
            // TODO #1
            throw new UnsupportedOperationException("convert from map to simple object not supported yet!");
        }
    }

    private void generateMethodWithOnlyReturnLine(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel,
        MapperCodeMetadata mapperGeneratedCodeMetadata,
        Consumer<MethodCodeMetadata> generatedMethodCodeMetadataConsumer,
        ObjectNodePath currentPath, String sourceValueExpression) {

        if (sourceMetaModel.isOnlyRawClassModel()) {
            var methodBuilder = MethodCodeMetadata.builder()
            .methodReturnType(targetMetaModel.getJavaGenericTypeInfo())
            .currentNodeType(sourceMetaModel.getJavaGenericTypeInfo())
            .methodName(sourceMetaModel, targetMetaModel);

            if (sourceMetaModel.getRealClass().equals(targetMetaModel.getRealClass())) {
                methodBuilder.lastLine(wrapValueWithReturnStatement(
                    targetMetaModel.getJavaGenericTypeInfo(),
                    sourceValueExpression));
            } else if (conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass())) {
                mapperGeneratedCodeMetadata.addConstructorArgument(ConversionService.class);

                var wrappedSourceValueExpression = String.format("conversionService.convert(%s, %s.class)",
                    sourceValueExpression, targetMetaModel.getCanonicalNameOfRealClass());

                methodBuilder.lastLine(wrapValueWithReturnStatement(
                    targetMetaModel.getJavaGenericTypeInfo(),
                    wrappedSourceValueExpression));
            } else {
                throw new TechnicalException(createMessagePlaceholder(
                    "mapper.converter.not.found.between.classes",
                    sourceMetaModel.getCanonicalNameOfRealClass(),
                    targetMetaModel.getCanonicalNameOfRealClass(),
                    currentPath.getFullPath()));
            }

            generatedMethodCodeMetadataConsumer.accept(methodBuilder.build());
        } else {
            // TODO #1
            throw new UnsupportedOperationException("convert from map to simple object not supported yet!");

        }
    }
}

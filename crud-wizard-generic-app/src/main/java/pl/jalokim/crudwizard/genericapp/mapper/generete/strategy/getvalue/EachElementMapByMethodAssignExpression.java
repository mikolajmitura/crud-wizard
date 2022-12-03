package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel.createOnlyOneMapperArguments;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGeneratorArgument;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MethodMetadataMapperConfig;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.TargetFieldMetaData;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Value
public class EachElementMapByMethodAssignExpression implements ValueToAssignExpression {

    String innerMethodName;

    ValueToAssignExpression wrappedExpression;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {

        var foundMethod = mapperGeneratedCodeMetadata
            .getMethodByName(innerMethodName);

        ClassMetaModel returnClassMetaModelForMappingElement = foundMethod.getReturnClassMetaModel();

        ValueToAssignCodeMetadata wrappedExpressionCodeMetaData = wrappedExpression.generateCodeMetadata(mapperGeneratedCodeMetadata);
        ClassMetaModel resultClassModelForWrappedExpression = wrappedExpressionCodeMetaData.getReturnClassModel();

        if (resultClassModelForWrappedExpression.isCollectionType() || resultClassModelForWrappedExpression.isArrayType()) {
            return getMappingCodeMetaDataForElements(mapperGeneratedCodeMetadata,
                resultClassModelForWrappedExpression, returnClassMetaModelForMappingElement);
        }

        return new MethodInCurrentClassAssignExpression(innerMethodName,
            List.of(wrappedExpression),
            returnClassMetaModelForMappingElement).generateCodeMetadata(mapperGeneratedCodeMetadata);
    }

    private ValueToAssignCodeMetadata getMappingCodeMetaDataForElements(MapperCodeMetadata mapperGeneratedCodeMetadata,
        ClassMetaModel resultClassModelForWrappedExpression, ClassMetaModel returnClassMetaModelForMappingElement) {

        Class<?> collectionClass = resultClassModelForWrappedExpression.getRealClass();
        ClassMetaModel returnClassMetaModelForNewMethod = ClassMetaModel.builder()
            .realClass(collectionClass)
            .genericTypes(List.of(returnClassMetaModelForMappingElement))
            .build();

        List<MapperArgumentMethodModel> newMethodArguments = createOnlyOneMapperArguments(resultClassModelForWrappedExpression);
        var methodGeneratorArgument = MapperMethodGeneratorArgument.builder()
            .methodName(createMethodName(newMethodArguments, returnClassMetaModelForNewMethod))
            .generated(true)
            .targetMetaModel(returnClassMetaModelForNewMethod)
            .mapperGeneratedCodeMetadata(mapperGeneratedCodeMetadata)
            .mapperConfiguration(MapperConfiguration.builder().build())
            .propertiesOverriddenMapping(PropertiesOverriddenMapping.builder().build())
            .mapperGenerateConfiguration(mapperGeneratedCodeMetadata.getMapperGenerateConfiguration())
            .currentPath(ObjectNodePath.rootNode())
            .parentMethodCodeMetadata(null)
            .matchedMethodFinder(findMethodArgument ->
                List.of(MethodMetadataMapperConfig.builder()
                    .methodName(innerMethodName)
                    .build()))
            .build();

        TargetFieldMetaData targetFieldMetaData = TargetFieldMetaData.builder()
            .fieldName("")
            .fieldNameNodePath(ObjectNodePath.rootNode())
            .targetFieldClassMetaModel(returnClassMetaModelForNewMethod)
            .build();

        AtomicReference<ValueToAssignExpression> generatedMappingCollectionMethodExpressionRef = new AtomicReference<>();

        mapperGeneratedCodeMetadata.getMapperMethodGenerator()
            .assignValueToFieldWithElements(methodGeneratorArgument,
                targetFieldMetaData,
                generatedMappingCollectionMethodExpressionRef,
                List.of(wrappedExpression));

        return generatedMappingCollectionMethodExpressionRef.get()
            .generateCodeMetadata(mapperGeneratedCodeMetadata);
    }
}

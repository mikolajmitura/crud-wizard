package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.createMethodName;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Value
@Builder(toBuilder = true)
public class MapperMethodGeneratorArgument {

    String methodName;
    boolean generated;
    List<MapperArgumentMethodModel> mapperMethodArguments;
    ClassMetaModel targetMetaModel;
    MapperCodeMetadata mapperGeneratedCodeMetadata;
    MapperConfiguration mapperConfiguration;
    PropertiesOverriddenMapping propertiesOverriddenMapping;
    MapperGenerateConfiguration mapperGenerateConfiguration;
    ObjectNodePath currentPath;
    MethodCodeMetadata parentMethodCodeMetadata;
    MapperMethodGeneratorArgument parentMapperMethodGeneratorArgument;

    @Builder.Default
    Function<FindMethodArgument, List<MethodMetadataMapperConfig>> matchedMethodFinder = findMethodArgument ->
        findMethodArgument.getMapperGeneratedCodeMetadata().findMatchNotGeneratedMethod(
            findMethodArgument.getTargetClassMetaModel(),
            findMethodArgument.getSourceClassMetaModel());

    MapperMethodGeneratorArgument createForNextMethod(List<MapperArgumentMethodModel> mapperMethodArguments,
        TargetFieldMetaData targetFieldMetaData) {

        return toBuilder()
            .methodName(createMethodName(mapperMethodArguments, targetFieldMetaData.getTargetFieldClassMetaModel()))
            .generated(true)
            .mapperMethodArguments(mapperMethodArguments)
            .targetMetaModel(targetFieldMetaData.getTargetFieldClassMetaModel())
            .propertiesOverriddenMapping(targetFieldMetaData.getPropertiesOverriddenMappingForField())
            .currentPath(targetFieldMetaData.getFieldNameNodePath())
            .build();
    }

    public List<ValueToAssignExpression> findOverriddenMappingStrategiesForCurrentNode() {
        return Optional.ofNullable(propertiesOverriddenMapping)
            .map(PropertiesOverriddenMapping::getValueMappingStrategy)
            .orElse(List.of());
    }

    public List<MethodMetadataMapperConfig> findMethodsFor(FindMethodArgument findMethodArgument) {
        return matchedMethodFinder.apply(findMethodArgument);
    }

    @Value
    public static class FindMethodArgument {

        MapperCodeMetadata mapperGeneratedCodeMetadata;
        ClassMetaModel targetClassMetaModel;
        ClassMetaModel sourceClassMetaModel;
    }
}

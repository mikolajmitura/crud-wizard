package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelForMapperHelper.getClassModelInfoForGeneratedCode;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel.createOnlyOneMapperArguments;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.EnumsMapperMethodGenerator;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGeneratorArgument;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.utils.template.TemplateAsText;

@Component
@RequiredArgsConstructor
public class MapperCodeGenerator {

    private final MapperMethodGenerator mapperMethodGenerator;
    private final EnumsMapperMethodGenerator enumsMapperMethodGenerator;

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
            .forEach((methodName, subMethodConfiguration) -> {

                    MapperMethodGeneratorArgument methodGeneratorArgument = MapperMethodGeneratorArgument.builder()
                        .methodName(subMethodConfiguration.getName())
                        .generated(false)
                        .mapperMethodArguments(createOnlyOneMapperArguments(subMethodConfiguration.getSourceMetaModel()))
                        .targetMetaModel(subMethodConfiguration.getTargetMetaModel())
                        .mapperGeneratedCodeMetadata(mapperGeneratedCodeMetadata)
                        .mapperConfiguration(subMethodConfiguration)
                        .propertiesOverriddenMapping(subMethodConfiguration.getPropertyOverriddenMapping())
                        .mapperGenerateConfiguration(mapperGenerateConfiguration)
                        .currentPath(ObjectNodePath.rootNode().nextNode("#" + subMethodConfiguration.getName() + "()"))
                        .parentMethodCodeMetadata(null)
                        .build();

                    if (subMethodConfiguration.isForMappingEnums()) {
                        enumsMapperMethodGenerator.creteEnumsMappingMethod(methodGeneratorArgument);
                    } else {
                        mapperGeneratedCodeMetadata.addOtherMethod(mapperMethodGenerator.generateMapperMethod(methodGeneratorArgument));
                    }
                }
            );

        mapperGeneratedCodeMetadata.setMainMethodCodeMetadata(
            mapperMethodGenerator.generateMapperMethod(MapperMethodGeneratorArgument.builder()
                .methodName("mainMethod")
                .generated(true)
                .mapperMethodArguments(List.of(new MapperArgumentMethodModel("sourceObject", sourceMetaModel)))
                .targetMetaModel(targetMetaModel)
                .mapperGeneratedCodeMetadata(mapperGeneratedCodeMetadata)
                .mapperConfiguration(mapperConfiguration)
                .propertiesOverriddenMapping(mapperConfiguration.getPropertyOverriddenMapping())
                .mapperGenerateConfiguration(mapperGenerateConfiguration)
                .currentPath(ObjectNodePath.rootNode())
                .parentMethodCodeMetadata(null)
                .build()
            ));

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
}

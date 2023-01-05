package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.utils.collection.Elements.elements;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.invoker.GeneratedMapperInvoker;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Value
public class ByMapperNameAssignExpression implements ValueToAssignExpression {

    public static final String GENERATED_MAPPER_INVOKER_BEAN_NAME = "generatedMapperInvoker";

    ClassMetaModel mapperReturnClassMetaModel;
    ValueToAssignExpression valueExpression;
    String mapperName;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {
        ValueToAssignCodeMetadata returnCodeMetadata = new ValueToAssignCodeMetadata();

        mapperGeneratedCodeMetadata.addConstructorArgument(GeneratedMapperInvoker.class, GENERATED_MAPPER_INVOKER_BEAN_NAME);
        mapperGeneratedCodeMetadata.addConstructorArgument(MetaModelContextService.class, "metaModelContextService");
        mapperGeneratedCodeMetadata.addImport(ClassMetaModel.class);
        returnCodeMetadata.setReturnClassModel(mapperReturnClassMetaModel);
        ValueToAssignCodeMetadata valueToAssignCodeMetadata = valueExpression.generateCodeMetadata(mapperGeneratedCodeMetadata);
        returnCodeMetadata.setValueGettingCode(String.format("%s.%s(\"%s\", %s, %s, %s, %s)", GENERATED_MAPPER_INVOKER_BEAN_NAME,
            "mapWithMapper", mapperName, "genericMapperArgument", valueToAssignCodeMetadata.getFullValueExpression(),
            generateFetchClassMetaModel(valueToAssignCodeMetadata.getReturnClassModel()), generateFetchClassMetaModel(mapperReturnClassMetaModel)));

        return returnCodeMetadata;
    }

    private String generateFetchClassMetaModel(ClassMetaModel classMetaModel) {
        if (classMetaModel.isGenericModel()) {
            return String.format("metaModelContextService.getClassMetaModelByName(\"%s\")", classMetaModel.getName());
        }
        return ClassMetaModelBuildExpression.builder()
            .realClass(classMetaModel.getRealOrBasedClass())
            .genericTypeExpressions(elements(classMetaModel.getGenericTypes())
                .map(this::generateFetchClassMetaModel)
                .asList()).build()
            .toString();
    }
}

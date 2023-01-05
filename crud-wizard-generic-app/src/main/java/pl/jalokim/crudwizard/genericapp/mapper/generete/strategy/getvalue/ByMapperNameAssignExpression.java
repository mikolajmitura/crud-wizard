package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.invoker.GeneratedMapperInvoker;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

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
        returnCodeMetadata.setReturnClassModel(mapperReturnClassMetaModel);
        returnCodeMetadata.setValueGettingCode(String.format("%s.%s(\"%s\", %s, %s)", GENERATED_MAPPER_INVOKER_BEAN_NAME,
            "mapWithMapper", mapperName, "genericMapperArgument", valueExpression.generateCodeMetadata(mapperGeneratedCodeMetadata)
                .getFullValueExpression()));

        return returnCodeMetadata;
    }
}

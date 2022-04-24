package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.GeneratedMapperInvoker;

@Value
public class ByMapperNameAssignExpression implements ValueToAssignExpression {

    public static final String GENERATED_MAPPER_INVOKER_BEAN_NAME = "generatedMapperInvoker";

    ClassMetaModel mapperReturnClassMetaModel;
    ValueToAssignExpression valueExpression;
    String mapperName;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata() {
        ValueToAssignCodeMetadata returnCodeMetadata = new ValueToAssignCodeMetadata();

        returnCodeMetadata.addConstructorArgument(GeneratedMapperInvoker.class, GENERATED_MAPPER_INVOKER_BEAN_NAME);
        returnCodeMetadata.setReturnClassModel(mapperReturnClassMetaModel);
        returnCodeMetadata.setValueGettingCode(String.format("%s.%s(\"%s\", %s, %s)", GENERATED_MAPPER_INVOKER_BEAN_NAME,
            "mapWithMapper", mapperName, "genericMapperArgument", valueExpression.generateCodeMetadata().getFullValueExpression()));

        return returnCodeMetadata;
    }
}

package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Value
public class MethodInCurrentClassAssignExpression implements ValueToAssignExpression {

    String methodName;
    List<ValueToAssignExpression> methodArgumentsExpressions;
    ClassMetaModel methodReturnType;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {

        String methodArgumentsAsText = StringUtils.EMPTY;
        if (isNotEmpty(methodArgumentsExpressions)) {
            methodArgumentsAsText = ", " + elements(methodArgumentsExpressions)
                .map(argument -> argument.generateCodeMetadata(mapperGeneratedCodeMetadata))
                .map(ValueToAssignCodeMetadata::getFullValueExpression)
                .asConcatText(", ");
        }

        return ValueToAssignCodeMetadata.builder()
            .valueGettingCode(String.format("%s(genericMapperArgument%s)", methodName, methodArgumentsAsText))
            .returnClassModel(methodReturnType)
            .build();
    }
}

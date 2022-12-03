package pl.jalokim.crudwizard.genericapp.mapper.generete;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.MapperGenerateConstants.SOURCE_OBJECT_VAR_NAME;

import java.util.List;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Value
public class MapperArgumentMethodModel {

    String argumentName;
    ClassMetaModel argumentType;
    ValueToAssignExpression derivedFromExpression;

    public MapperArgumentMethodModel overrideType(ClassMetaModel type, ValueToAssignExpression derivedFromExpression) {
        return new MapperArgumentMethodModel(argumentName, type, derivedFromExpression);
    }

    public static List<MapperArgumentMethodModel> createOnlyOneMapperArguments(ClassMetaModel argumentType) {
        return List.of(new MapperArgumentMethodModel(SOURCE_OBJECT_VAR_NAME, argumentType, null));
    }

}

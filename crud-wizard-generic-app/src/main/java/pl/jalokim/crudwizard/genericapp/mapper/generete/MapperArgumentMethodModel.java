package pl.jalokim.crudwizard.genericapp.mapper.generete;

import java.util.List;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
public class MapperArgumentMethodModel {

    String argumentName;
    ClassMetaModel argumentType;

    public MapperArgumentMethodModel overrideType(ClassMetaModel type) {
        return new MapperArgumentMethodModel(argumentName, type);
    }

    public static List<MapperArgumentMethodModel> createOnlyOneMapperArguments(ClassMetaModel argumentType) {
        return List.of(new MapperArgumentMethodModel("sourceObject", argumentType));
    }

}

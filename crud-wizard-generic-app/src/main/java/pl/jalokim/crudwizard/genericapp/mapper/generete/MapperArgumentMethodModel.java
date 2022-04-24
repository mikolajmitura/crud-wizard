package pl.jalokim.crudwizard.genericapp.mapper.generete;

import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
public class MapperArgumentMethodModel {

    String argumentName;
    ClassMetaModel argumentType;

    public MapperArgumentMethodModel overrideType(ClassMetaModel type) {
        return new MapperArgumentMethodModel(argumentName, type);
    }

}

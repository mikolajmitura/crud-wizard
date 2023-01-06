package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;

@Value
public class MethodParameterInfo {

    JavaTypeMetaModel argumentMetaModel;
    int index;
    String name;
}

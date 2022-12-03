package pl.jalokim.crudwizard.genericapp.metamodel.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodArgumentMetaModel {

    List<Annotation> annotations;

    JavaTypeMetaModel argumentType;

    Parameter parameter;

}

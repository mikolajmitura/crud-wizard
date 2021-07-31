package pl.jalokim.crudwizard.core.metamodels;

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

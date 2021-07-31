package pl.jalokim.crudwizard.core.metamodels;

import java.lang.annotation.Annotation;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodArgumentMetaModel {

    Set<Annotation> annotations;

    JavaTypeMetaModel argumentType;
}

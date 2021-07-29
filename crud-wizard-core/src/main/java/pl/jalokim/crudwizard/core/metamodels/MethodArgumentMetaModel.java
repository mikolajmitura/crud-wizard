package pl.jalokim.crudwizard.core.metamodels;

import java.lang.annotation.Annotation;
import java.util.Set;
import lombok.Value;
import pl.jalokim.utils.reflection.TypeMetadata;

@Value
public class MethodArgumentMetaModel {

    Set<Annotation> annotations;

    TypeMetadata argumentType;
    // TODO #0 'argumentType' some class meta info
    // maybe instead of TypeMetadata from java-utils use from generics-resolver
}

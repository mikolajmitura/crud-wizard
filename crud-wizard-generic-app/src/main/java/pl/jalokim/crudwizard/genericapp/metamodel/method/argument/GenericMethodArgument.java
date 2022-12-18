package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class GenericMethodArgument {

    @Builder.Default
    List<TypePredicateAndDataExtractor> typePredicatesAndDataExtractors = new ArrayList<>();
    Class<? extends Annotation> annotatedWith;
    Consumer<ResolvedValueForAnnotation> resolvedValueValidator;

    /**
     * Can be input of mapper only when one of typePredicates is passed and when is annotated with annotatedWith
     */
    boolean argumentCanBeInputOfMapper;
}

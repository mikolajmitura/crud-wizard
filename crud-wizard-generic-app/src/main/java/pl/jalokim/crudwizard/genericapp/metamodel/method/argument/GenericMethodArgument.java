package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class GenericMethodArgument {

    List<TypePredicateAndDataExtractor> typePredicatesAndDataExtractors;
    Class<? extends Annotation> annotatedWith;
    Function<ResolvedValueForAnnotation, Object> resolvedValueValidator;

    /**
     * Can be input of mapper only when one of typePredicates is passed and when is annotated with annotatedWith
     */
    boolean argumentCanBeInputOfMapper;

    private GenericMethodArgument(
        List<TypePredicateAndDataExtractor> typePredicatesAndDataExtractors, Class<? extends Annotation> annotatedWith,
        Function<ResolvedValueForAnnotation, Object> resolvedValueValidator, boolean argumentCanBeInputOfMapper) {

        this.typePredicatesAndDataExtractors = typePredicatesAndDataExtractors;
        this.annotatedWith = annotatedWith;
        this.resolvedValueValidator = resolvedValueValidator;
        this.argumentCanBeInputOfMapper = argumentCanBeInputOfMapper;

        elements(typePredicatesAndDataExtractors)
            .forEach(typePredicateAndDataExtractor -> typePredicateAndDataExtractor.setupBelongsTo(this));
    }
}

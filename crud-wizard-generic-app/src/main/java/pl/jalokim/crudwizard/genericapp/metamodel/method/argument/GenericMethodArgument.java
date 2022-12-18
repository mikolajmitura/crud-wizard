package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import pl.jalokim.utils.collection.Elements;

@Builder(toBuilder = true)
@Getter
public class GenericMethodArgument {

    @Builder.Default
    List<TypePredicateAndDataExtractor> typePredicatesAndDataExtractors = new ArrayList<>();
    Class<? extends Annotation> annotatedWith;
    Function<ResolvedValueForAnnotation, Object> resolvedValueValidator;

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

    /**
     * Can be input of mapper only when one of typePredicates is passed and when is annotated with annotatedWith
     */
    boolean argumentCanBeInputOfMapper;

    String description() {
        return Elements.of("annotatedWith: " + annotatedWith,
            "typePredicatesAndDataExtractors: " + System.lineSeparator() + Elements.elements(typePredicatesAndDataExtractors)
                .map(element -> "subTypeOf: " + element.getSubTypeOf().getTypeDescription())
                .concatWithNewLines())
            .concatWithNewLines();
    }
}

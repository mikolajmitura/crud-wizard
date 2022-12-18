package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Builder
@Value
@Getter
public class TypePredicateAndDataExtractor {

    @NotNull
    ClassMetaModel subTypeOf;

    @NotNull
    Function<ArgumentValueExtractMetaModel, Object> extractDataFunction;

    @Builder.Default
    List<ClassMetaModelsPredicate> predicatesOfModel = List.of();

    static TypePredicateAndDataExtractor newTypePredicate(Class<?> isSubTypeOf,
        Function<ArgumentValueExtractMetaModel, Object> extractDataFunction,
        ClassMetaModelsPredicate... predicateOfType) {
        return newTypePredicate(ClassMetaModel.builder()
            .realClass(isSubTypeOf)
            .build(), extractDataFunction, predicateOfType);
    }

    @SuppressWarnings("unchecked")
    static <P extends GenericMethodArgumentProvider> TypePredicateAndDataExtractor newTypePredicateAndDataProvide(Class<?> isSubTypeOf,
        Function<P, Object> extractDataFunction,
        ClassMetaModelsPredicate... predicateOfType) {
        return newTypePredicate(ClassMetaModel.builder()
                .realClass(isSubTypeOf)
                .build(),
            argumentValueExtractMetaModel ->
            {
                Object value = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
                P provider = (P) value;
                return extractDataFunction.apply(provider);
            },
            predicateOfType);
    }

    static TypePredicateAndDataExtractor newTypePredicate(ClassMetaModel isSubTypeOf,
        Function<ArgumentValueExtractMetaModel, Object> extractDataFunction,
        ClassMetaModelsPredicate... predicateOfType) {
        return TypePredicateAndDataExtractor.builder()
            .subTypeOf(isSubTypeOf)
            .extractDataFunction(extractDataFunction)
            .predicatesOfModel(elements(predicateOfType).asList())
            .build();
    }
}

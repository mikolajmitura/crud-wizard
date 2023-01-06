package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;

@Slf4j
@UtilityClass
public class TypePredicateAndDataExtractorResolver {

    @SuppressWarnings("PMD.UnusedAssignment")
    public static TypePredicateAndDataExtractor findTypePredicateAndDataExtractor(List<GenericMethodArgument> methodExpectedArguments,
        ClassMetaModel typeOfInputServiceOrMapper, MethodArgumentMetaModel methodArgument,
        ClassMetaModel classMetaModelFromMethodArg, EndpointQueryAndUrlMetaModel endpointQueryAndUrlMetaModel) {

        TypePredicateAndDataExtractor foundTypePredicateAndDataExtractor = null;
        for (GenericMethodArgument expectedMethodArgument : methodExpectedArguments) {

            Class<?> annotatedWith = expectedMethodArgument.getAnnotatedWith();
            List<TypePredicateAndDataExtractor> typePredicates = expectedMethodArgument.getTypePredicatesAndDataExtractors();
            log.debug("expectedMethodArgument isAnnotatedWith: {}", annotatedWith);
            log.debug("expectedMethodArgument typePredicates size: {}", typePredicates.size());

            boolean annotatedWithWasFound = false;
            AtomicInteger index = new AtomicInteger();
            AtomicReference<TypePredicateAndDataExtractor> foundTypePredicateAndDataExtractorRef = new AtomicReference<>();

            if ((annotatedWith == null ||
                (annotatedWithWasFound = elements(methodArgument.getAnnotations())
                    .map(Annotation::annotationType)
                    .asList()
                    .contains(annotatedWith))) &&
                isFoundAnyMatchedTypePredicate(typeOfInputServiceOrMapper, methodArgument, classMetaModelFromMethodArg,
                    endpointQueryAndUrlMetaModel, typePredicates, index, foundTypePredicateAndDataExtractorRef)) {
                log.debug("foundExpectedMethodArgType set to true");
                foundTypePredicateAndDataExtractor = foundTypePredicateAndDataExtractorRef.get();
                break;
            }

            if (annotatedWithWasFound) {
                log.debug("foundExpectedMethodArgType set to false due to annotatedWithWasFound=true");
                foundTypePredicateAndDataExtractor = null;
                break;
            }
        }
        return foundTypePredicateAndDataExtractor;
    }

    private static boolean isFoundAnyMatchedTypePredicate(ClassMetaModel typeOfInputServiceOrMapper, MethodArgumentMetaModel methodArgument,
        ClassMetaModel classMetaModelFromMethodArg, EndpointQueryAndUrlMetaModel endpointQueryAndUrlMetaModel,
        List<TypePredicateAndDataExtractor> typePredicates, AtomicInteger index,
        AtomicReference<TypePredicateAndDataExtractor> foundTypePredicateAndDataExtractorRef) {

        elements(typePredicates)
            .filter(typePredicate -> {
                    log.debug("checking predicate at index: {}", index.incrementAndGet());
                    boolean subTypeOfResult = classMetaModelFromMethodArg
                        .isSubTypeOf(typePredicate.getSubTypeOf());

                    log.debug("classMetaModelFromMethodArg is sub type of {}, result: {}",
                        typePredicate.getSubTypeOf().getTypeDescription(), subTypeOfResult);

                    boolean predicatesAllEmptyOrAllMatch = typePredicate.getPredicatesOfModel().isEmpty() ||
                        typePredicate.getPredicatesOfModel().stream().allMatch(
                            predicateClass -> predicateClass.test(
                                methodArgument,
                                classMetaModelFromMethodArg,
                                typeOfInputServiceOrMapper,
                                endpointQueryAndUrlMetaModel
                            ));

                    log.debug("predicatesAllEmptyOrAllMatch: {}", predicatesAllEmptyOrAllMatch);

                    return subTypeOfResult &&
                        predicatesAllEmptyOrAllMatch;
                }
            ).findFirst()
            .ifPresent(foundTypePredicateAndDataExtractorRef::set);

        return foundTypePredicateAndDataExtractorRef.get() != null;
    }
}

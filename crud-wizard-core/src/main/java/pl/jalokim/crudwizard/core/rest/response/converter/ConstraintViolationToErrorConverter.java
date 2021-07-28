package pl.jalokim.crudwizard.core.rest.response.converter;

import static javax.validation.ElementKind.BEAN;
import static javax.validation.ElementKind.CONTAINER_ELEMENT;
import static javax.validation.ElementKind.METHOD;
import static javax.validation.ElementKind.PARAMETER;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;
import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto;

@UtilityClass
public class ConstraintViolationToErrorConverter {

    private static final Set<ElementKind> IGNORED_ELEMENT_KINDS = Set.of(BEAN, METHOD, PARAMETER, CONTAINER_ELEMENT);

    public static ErrorDto toErrorDto(ConstraintViolation<?> constraintViolation) {
        return ErrorDto.builder()
            .property(extractPropertyPath(constraintViolation.getPropertyPath()))
            .message(constraintViolation.getMessage())
            .build();
    }

    private static String extractPropertyPath(Path propertyPath) {
        return StreamSupport.stream(propertyPath.spliterator(), false)
            .filter(Predicate.not(ConstraintViolationToErrorConverter::shouldBeIgnored))
            .map(Node::toString)
            .collect(Collectors.joining("."));
    }

    private static boolean shouldBeIgnored(Node node) {
        return IGNORED_ELEMENT_KINDS.contains(node.getKind());
    }
}

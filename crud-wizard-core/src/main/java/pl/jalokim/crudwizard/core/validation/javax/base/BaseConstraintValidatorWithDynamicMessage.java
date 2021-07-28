package pl.jalokim.crudwizard.core.validation.javax.base;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;

public interface BaseConstraintValidatorWithDynamicMessage<A extends Annotation, T> extends BaseConstraintValidator<A, T> {

    @Override
    default boolean isValid(T value, ConstraintValidatorContext context) {
        var result = isValidWithoutCustomMessage(value, context);
        setupCustomMessage(value, context);
        return result;
    }

    default boolean isValidWithoutCustomMessage(T value, ConstraintValidatorContext context) {
        return BaseConstraintValidator.super.isValid(value, context);
    }

    default void setupCustomMessage(T value, ConstraintValidatorContext context) {
        customMessage(context, MessagePlaceholder.createMessagePlaceholder(
            messagePlaceholder(context), messagePlaceholderArgs(value, context)
        ));
    }

    default Map<String, Object> messagePlaceholderArgs(T value, ConstraintValidatorContext context) {
        return Map.of();
    }

    default String messagePlaceholder(ConstraintValidatorContext context) {
        Class<? extends Annotation> validatorAnnotation = ((ConstraintValidatorContextImpl) context).getConstraintDescriptor().getAnnotation()
            .annotationType();
        return buildMessageForValidator(validatorAnnotation);
    }

    static String buildMessageForValidator(Class<? extends Annotation> validatorAnnotation) {
        return String.format("%s.message", validatorAnnotation.getCanonicalName());
    }

    static String joinWithComma(String... fieldNames) {
        return joinWithComma(Arrays.asList(fieldNames));
    }

    static String joinWithComma(List<String> fieldNames) {
        return String.join(", ", fieldNames);
    }

    static String joinFieldNames(String... fieldNames) {
        return joinFieldNames(Arrays.asList(fieldNames));
    }

    static String joinFieldNames(List<String> fieldNames) {
        return fieldNames.stream()
            .map(MessagePlaceholder::wrapAsExternalPlaceholder)
            .collect(Collectors.joining(", "));
    }
}

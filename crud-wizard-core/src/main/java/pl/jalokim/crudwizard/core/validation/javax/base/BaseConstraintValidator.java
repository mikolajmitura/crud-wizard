package pl.jalokim.crudwizard.core.validation.javax.base;

import java.lang.annotation.Annotation;
import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath.PropertyPathBuilder;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath.PropertyPathPart;

public interface BaseConstraintValidator<A extends Annotation, T> extends ConstraintValidator<A, T> {

    boolean isValidValue(T value, ConstraintValidatorContext context);

    @Override
    default boolean isValid(T value, ConstraintValidatorContext context) {
        return Objects.isNull(value) || isValidValue(value, context);
    }

    default void addMessageVariable(ConstraintValidatorContext context, String name, Object value) {
        context.unwrap(HibernateConstraintValidatorContext.class)
            .addExpressionVariable(name, value);
    }

    /**
     * For translate some given placeholder in message.
     *
     * @param context - validation context
     * @param name - name of variable in message with placeholder
     * @param value - value which should be added under some placeholder
     */
    default void addMessageParameter(ConstraintValidatorContext context, String name, Object value) {
        context.unwrap(HibernateConstraintValidatorContext.class)
            .addMessageParameter(name, value);
    }

    /**
     * Override default message for validator with custom message or message placeholder
     */
    default void customMessage(ConstraintValidatorContext context, String messageOrPlaceholder) {
        customMessage(context, messageOrPlaceholder, PropertyPath.builder().build());
    }

    /**
     * Override default message for validator with custom message via {@link MessagePlaceholder} instance
     */
    default void customMessage(ConstraintValidatorContext context, MessagePlaceholder messagePlaceholder) {
        customMessage(context, messagePlaceholder.translateMessage(), PropertyPath.builder().build());
    }

    /**
     * Override default message for validator with custom message or message placeholder and attach it to certain bean property name.
     */
    default void customMessage(ConstraintValidatorContext context, String messageOrPlaceholder, String propertyNodeName) {
        customMessage(context, messageOrPlaceholder, PropertyPath.builder().addNextProperty(propertyNodeName).build());
    }

    /**
     * Override default message for validator with custom message via {@link MessagePlaceholder}
     * attach it to certain bean property via builder {@link PropertyPathBuilder}.
     */
    default void customMessage(ConstraintValidatorContext context, MessagePlaceholder messagePlaceholder, PropertyPath propertyPath) {
        customMessage(context, messagePlaceholder.translateMessage(), propertyPath);
    }

    /**
     * Override default message for validator with custom message or message placeholder and
     * attach it to certain bean property via builder {@link PropertyPathBuilder}.
     */
    default void customMessage(ConstraintValidatorContext context, String messageOrPlaceholder, PropertyPath propertyPath) {
        context.disableDefaultConstraintViolation();
        var constraintViolationBuilder = context.buildConstraintViolationWithTemplate(messageOrPlaceholder);

        CurrentValidationContextWrapper currentPathHelper = CurrentValidationContextWrapper.builder()
            .constraintViolationBuilder(constraintViolationBuilder)
            .build();

        for (PropertyPathPart propertyPart : propertyPath.getPropertyParts()) {
            currentPathHelper = currentPathHelper.addPropertyNode(propertyPart.getPropertyName());

            if (propertyPart.isListTypeProperty()) {
                currentPathHelper = currentPathHelper.addComposedPropertyNode(propertyPart.getPropertyName(), propertyPart.getIndex());
            }
        }
        currentPathHelper.addConstraintViolation();
    }
}

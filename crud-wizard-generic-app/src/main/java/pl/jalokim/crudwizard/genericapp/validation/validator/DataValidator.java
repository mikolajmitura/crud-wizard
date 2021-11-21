package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public interface DataValidator<T> {

    String NAME_FORMAT = "%s.name";
    String MESSAGE_FORMAT = "%s.message";

    /**
     * It returns translation property key for name of validator
     *
     * @return translation key for name of validator
     */
    default String namePlaceholder() {
        return String.format(NAME_FORMAT, this.getClass().getCanonicalName());
    }

    /**
     * It returns translation property key for message after fault validation.
     *
     * @return translation key for validation message.
     */
    default String messagePlaceholder() {
        return formatPlaceholderFor(this.getClass());
    }

    default Map<String, Object> messagePlaceholderArgs(ValidationSessionContext validationContext) {
        return validationContext.getValidatorModelContext().fetchMessagePlaceholders();
    }

    static String formatPlaceholderFor(Class<?> type) {
        return String.format(MESSAGE_FORMAT, type.getCanonicalName());
    }

    /**
     * Entrpoint method for invoke validation on some obeject.
     */
    boolean isValid(T value, ValidationSessionContext validationContext);

    /**
     * This method returns types of classes which can validated via some data validator.
     * By default it gets class from T generic type if can resolve.
     * @see T
     */
    default List<Class<?>> getTypesToValidate() {
        try {
            return List.of(getTypeMetadataFromClass(getClass())
                .getTypeMetaDataForParentClass(DataValidator.class)
                .getGenericTypes().get(0).getRawType());

        } catch (NoSuchElementException ex) {
            throw new IllegalStateException("cannot find class which can be validated by validator: " + this.getClass().getCanonicalName(), ex);
        }
    }

    /**
     * Additional method to check that validator can validate some object in some certain type.
     */
    default boolean canValidate(Class<?> typeToVerify) {
        List<Class<?>> typesToValidate = getTypesToValidate();
        boolean canValidate = false;
        for (Class<?> typeToValidate : typesToValidate) {
            if (isTypeOf(typeToVerify, typeToValidate)) {
                canValidate = true;
                break;
            }
        }
        return canValidate;
    }

    /**
     * It should return unique validator name, this should be like validator key.
     */
    String validatorName();

}



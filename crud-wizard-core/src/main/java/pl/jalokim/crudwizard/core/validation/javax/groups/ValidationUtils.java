package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.experimental.UtilityClass;
import pl.jalokim.utils.collection.CollectionUtils;

@UtilityClass
public class ValidationUtils {

    public static <T> void validateBean(Validator delegator, T bean, Class<?>... groups) {
        Set<ConstraintViolation<T>> validationResults = getValidationErrors(delegator, bean, groups);
        if (CollectionUtils.isNotEmpty(validationResults)) {
            throw new ConstraintViolationException(validationResults);
        }
    }

    public static <T> Set<ConstraintViolation<T>> getValidationErrors(Validator delegator, T bean, Class<?>... groups) {
        return delegator.validate(bean, groups);
    }
}

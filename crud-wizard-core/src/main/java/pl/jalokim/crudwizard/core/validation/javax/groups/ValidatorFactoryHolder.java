package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import pl.jalokim.utils.collection.CollectionUtils;

@Configuration
public class ValidatorFactoryHolder {

    private static final AtomicReference<ValidatorFactory> VALIDATOR_FACTORY_REFERENCE = new AtomicReference<>();

    @Autowired
    private ValidatorFactory validatorFactory;

    @PostConstruct
    public void init() {
        setValidatorFactory(validatorFactory);
    }

    public static ValidatorFactory getValidatorFactory() {
        return Optional.ofNullable(VALIDATOR_FACTORY_REFERENCE.get())
            .orElseThrow(() -> new NoSuchElementException("VALIDATOR_FACTORY_REFERENCE is not set"));
    }

    public static <T> void validateBean(Validator delegator, T bean, Class<?>... groups) {
        Set<ConstraintViolation<T>> validationResults = getValidationErrors(delegator, bean, groups);
        if (CollectionUtils.isNotEmpty(validationResults)) {
            throw new ConstraintViolationException(validationResults);
        }
    }

    public static <T> Set<ConstraintViolation<T>> getValidationErrors(Validator delegator, T bean, Class<?>... groups) {
        Set<ConstraintViolation<T>> validationResults = new HashSet<>(delegator.validate(bean));
        if (groups.length > 0) {
            validationResults.addAll(delegator.validate(bean, groups));
        }
        return validationResults;
    }

    private static void setValidatorFactory(ValidatorFactory validatorFactory) {
        VALIDATOR_FACTORY_REFERENCE.set(validatorFactory);
    }
}

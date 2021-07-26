package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

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

    private static void setValidatorFactory(ValidatorFactory validatorFactory) {
        VALIDATOR_FACTORY_REFERENCE.set(validatorFactory);
    }
}

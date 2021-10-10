package pl.jalokim.crudwizard.genericapp.validation.validator;

import java.util.Map;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class EmailValidator extends JavaxProxyDataValidator<Email, CharSequence> {

    public static final String EMAIL = "EMAIL";

    public static final Map<Class<?>, Class<?>> EMAIL_VALIDATOR_MAP = Map
        .of(CharSequence.class, org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator.class);

    @Override
    public String validatorName() {
        return EMAIL;
    }

    @Override
    public Map<Class<?>, Class<?>> getValidatorsByType() {
        return EMAIL_VALIDATOR_MAP;
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(ValidationSessionContext validationContext) {
        return Map.of(
            "regexp", ".*",
            "flags", new Pattern.Flag[0]
        );
    }
}

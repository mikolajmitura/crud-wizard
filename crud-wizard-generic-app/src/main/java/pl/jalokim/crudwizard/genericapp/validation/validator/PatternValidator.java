package pl.jalokim.crudwizard.genericapp.validation.validator;

import java.util.Map;
import javax.validation.constraints.Pattern;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class PatternValidator extends JavaxProxyDataValidator<Pattern, CharSequence> {

    public static final String VALIDATOR_KEY_PATTERN = "PATTERN";
    public static final String PATTERN_ARG_NAME = "regexp";

    public static final Map<Class<?>, Class<?>> PATTERN_VALIDATOR_MAP = Map
        .of(CharSequence.class, org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator.class);

    @Override
    public String validatorName() {
        return VALIDATOR_KEY_PATTERN;
    }

    @Override
    public Map<Class<?>, Class<?>> getValidatorsByType() {
        return PATTERN_VALIDATOR_MAP;
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(ValidationSessionContext validationContext) {
        return Map.of(
            "regexp", validationContext.getValidatorArgument(PATTERN_ARG_NAME),
            "flags", new Pattern.Flag[0]
        );
    }
}

package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator.formatPlaceholderFor;

import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class NotBlankValidator implements DataValidator<CharSequence> {

    public static final String VALIDATOR_NAME = "NOT_BLANK";

    @Override
    public boolean isValid(CharSequence value, ValidationSessionContext validationContext) {
        return StringUtils.isNotBlank(value);
    }

    @Override
    public String validatorName() {
        return VALIDATOR_NAME;
    }

    @Override
    public String messagePlaceholder() {
        return formatPlaceholderFor(NotBlank.class);
    }
}

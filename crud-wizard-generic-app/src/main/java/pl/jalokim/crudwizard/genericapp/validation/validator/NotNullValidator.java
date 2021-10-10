package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator.formatPlaceholderFor;

import javax.validation.constraints.NotNull;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class NotNullValidator implements DataValidator<Object> {

    public static final String NOT_NULL = "NOT_NULL";

    @Override
    public boolean isValid(Object value, ValidationSessionContext validationContext) {
        return value != null;
    }

    @Override
    public String validatorName() {
        return NOT_NULL;
    }

    @Override
    public String messagePlaceholder() {
        return formatPlaceholderFor(NotNull.class);
    }
}

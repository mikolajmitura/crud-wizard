package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator.formatPlaceholderFor;

import javax.validation.constraints.Null;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class NullValidator implements DataValidator<Object> {

    public static final String NULL = "NULL";

    @Override
    public boolean isValid(Object value, ValidationSessionContext validationContext) {
        return value == null;
    }

    @Override
    public String validatorName() {
        return NULL;
    }

    @Override
    public String messagePlaceholder() {
        return formatPlaceholderFor(Null.class);
    }
}

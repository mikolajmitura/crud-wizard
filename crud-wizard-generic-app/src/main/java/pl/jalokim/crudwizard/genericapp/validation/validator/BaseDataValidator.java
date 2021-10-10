package pl.jalokim.crudwizard.genericapp.validation.validator;

import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public abstract class BaseDataValidator<T> implements DataValidator<T> {

    @Override
    public boolean isValid(T value, ValidationSessionContext validationContext) {
        if (value != null) {
            return hasValidValue(value, validationContext);
        }
        return true;
    }

    protected abstract boolean hasValidValue(T value, ValidationSessionContext validationContext);
}

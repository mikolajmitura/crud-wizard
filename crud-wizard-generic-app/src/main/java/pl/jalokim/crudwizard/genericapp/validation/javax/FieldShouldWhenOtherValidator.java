package pl.jalokim.crudwizard.genericapp.validation.javax;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;

public class FieldShouldWhenOtherValidator implements BaseConstraintValidator<FieldShouldWhenOther, Object> {

    @Override
    public void initialize(FieldShouldWhenOther constraintAnnotation) {

    }

    @Override
    public boolean isValidValue(Object value, ConstraintValidatorContext context) {
        return false;
    }

}

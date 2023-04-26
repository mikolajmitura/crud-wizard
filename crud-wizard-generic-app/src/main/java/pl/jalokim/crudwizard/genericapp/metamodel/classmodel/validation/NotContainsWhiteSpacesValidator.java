package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;

public class NotContainsWhiteSpacesValidator implements BaseConstraintValidatorWithDynamicMessage<NotContainsWhiteSpaces, String> {

    @Override
    public boolean isValidValue(String value, ConstraintValidatorContext context) {
        return value.replaceAll("\\s", "").length() == value.length();
    }
}

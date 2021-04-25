package pl.jalokim.crudwizard.core.validation.javax.groups;

import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class ValidatorWithDefaultGroupFactoryBean extends LocalValidatorFactoryBean {

    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {
        if (validationHints.length > 0) {
            super.validate(target, errors, validationHints);
        }
        super.validate(target, errors);
    }
}

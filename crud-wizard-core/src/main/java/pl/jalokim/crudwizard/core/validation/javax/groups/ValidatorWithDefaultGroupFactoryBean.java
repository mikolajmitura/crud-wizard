package pl.jalokim.crudwizard.core.validation.javax.groups;

import javax.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;

public class ValidatorWithDefaultGroupFactoryBean extends LocalValidatorFactoryBean {

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        InvokableReflectionUtils.invokeMethod(this, "setTargetValidator", createValidatorWithDefaultGroupsWrapper());
    }

    @Override
    public Validator getValidator() {
        return createValidatorWithDefaultGroupsWrapper();
    }

    private Validator createValidatorWithDefaultGroupsWrapper() {
        return new ValidatorWithDefaultGroupsWrapper(super.getValidator());
    }

}

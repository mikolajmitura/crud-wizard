package pl.jalokim.crudwizard.test.utils.validation

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorFactory
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl

/**
 * This for test purposes when javax validators has injected spring services.
 * This TestingConstraintValidatorFactory class allows to provide own implementation of validators.
 */
class TestingConstraintValidatorFactory implements ConstraintValidatorFactory {

    private def constraintValidatorFactory = new ConstraintValidatorFactoryImpl()
    List<?> validatorsDependencies

    TestingConstraintValidatorFactory(Object... validatorsDependencies) {
        this.validatorsDependencies = validatorsDependencies
    }

    static Validator createTestingValidator(Object... validatorsDependencies) {
        new ValidatorWithDefaultGroupsWrapper(createTestingValidatorFactory(new AppMessageSourceTestImpl(), validatorsDependencies).getValidator())
    }

    static Validator createTestingValidator(AppMessageSourceTestImpl messageSource, Object... validatorsDependencies) {
        new ValidatorWithDefaultGroupsWrapper(createTestingValidatorFactory(messageSource, validatorsDependencies).getValidator())
    }

    static ValidatorFactory createTestingValidatorFactory(AppMessageSourceTestImpl messageSource, Object... validatorsDependencies) {
        Validation.byDefaultProvider()
            .configure()
            .constraintValidatorFactory(new TestingConstraintValidatorFactory(validatorsDependencies))
            .messageInterpolator(LocalValidatorFactoryBean.HibernateValidatorDelegate.buildMessageInterpolator(messageSource.getMessageSource()))
            .buildValidatorFactory()
    }

    @Override
    def <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> validatorClass) {
        return BeanCreationArgsResolver.createInstance(validatorClass, validatorsDependencies)
    }

    @Override
    void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {
        constraintValidatorFactory.releaseInstance(constraintValidator)
    }
}

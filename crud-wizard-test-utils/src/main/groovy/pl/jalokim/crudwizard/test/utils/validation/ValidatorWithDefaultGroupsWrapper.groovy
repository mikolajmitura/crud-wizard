package pl.jalokim.crudwizard.test.utils.validation

import javax.validation.ConstraintViolation
import javax.validation.Validator
import javax.validation.executable.ExecutableValidator
import javax.validation.metadata.BeanDescriptor

class ValidatorWithDefaultGroupsWrapper implements Validator {

    ValidatorWithDefaultGroupsWrapper(Validator delegated) {
        this.delegated = delegated
    }

    private final Validator delegated

    @Override
    def <T> Set<ConstraintViolation<T>> validate(T t, Class<?>... classes) {
        def results = [] as Set
        if (classes.length > 0) {
            results.addAll(delegated.validate(t, classes))
        }
        results.addAll(delegated.validate(t))
        return results
    }

    @Override
    def <T> Set<ConstraintViolation<T>> validateProperty(T t, String s, Class<?>... classes) {
        return delegated.validateProperty(t, s, classes)
    }

    @Override
    def <T> Set<ConstraintViolation<T>> validateValue(Class<T> aClass, String s, Object o, Class<?>... classes) {
        return delegated.validateValue(aClass, s, o, classes)
    }

    @Override
    BeanDescriptor getConstraintsForClass(Class<?> aClass) {
        return delegated.getConstraintsForClass(aClass)
    }

    @Override
    def <T> T unwrap(Class<T> aClass) {
        return delegated.unwrap(aClass)
    }

    @Override
    ExecutableValidator forExecutables() {
        return delegated.forExecutables()
    }
}

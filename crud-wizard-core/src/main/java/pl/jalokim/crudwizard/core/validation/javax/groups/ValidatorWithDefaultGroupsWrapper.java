package pl.jalokim.crudwizard.core.validation.javax.groups;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import pl.jalokim.utils.collection.Elements;

public class ValidatorWithDefaultGroupsWrapper implements Validator {

    private final Validator delegated;

    ValidatorWithDefaultGroupsWrapper(Validator delegated) {
        this.delegated = delegated;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
        List<Class<?>> groupsAsList = Elements.elements(groups)
            .asList();

        if (groupsAsList.contains(WithoutDefaultGroup.class) && groups.length > 1) {

            Class<?>[] onlyGivenGroups = Elements.elements(groupsAsList)
                .filter(group -> group != (WithoutDefaultGroup.class))
                .asArray(new Class<?>[]{});

            return new HashSet<>(delegated.validate(object, onlyGivenGroups));
        }

        Set<ConstraintViolation<T>> validationResults = new HashSet<>(delegated.validate(object));
        if (groups.length > 0) {
            validationResults.addAll(delegated.validate(object, groups));
        }
        return validationResults;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
        return delegated.validateProperty(object, propertyName, groups);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        return delegated.validateValue(beanType, propertyName, value, groups);
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        return delegated.getConstraintsForClass(clazz);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return delegated.unwrap(type);
    }

    @Override
    public ExecutableValidator forExecutables() {
        return delegated.forExecutables();
    }
}

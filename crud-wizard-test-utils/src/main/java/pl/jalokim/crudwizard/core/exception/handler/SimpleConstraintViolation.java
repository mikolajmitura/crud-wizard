package pl.jalokim.crudwizard.core.exception.handler;

import groovy.transform.NamedVariant;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

class SimpleConstraintViolation implements ConstraintViolation {

    @NamedVariant
    SimpleConstraintViolation(Path propertyPath, String message) {
        this.propertyPath = propertyPath;
        this.message = message;
    }

    Path propertyPath;
    String message;
    final String messageTemplate = null;
    final Object rootBean = null;
    final Class rootBeanClass = null;
    final Object leafBean = null;
    final Object executableReturnValue = null;
    final Object invalidValue = null;
    final ConstraintDescriptor<?> constraintDescriptor = null;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessageTemplate() {
        return null;
    }

    @Override
    public Object getRootBean() {
        return null;
    }

    @Override
    public Class getRootBeanClass() {
        return null;
    }

    @Override
    public Object getLeafBean() {
        return null;
    }

    @Override
    public Object[] getExecutableParameters() {
        return new Object[0];
    }

    @Override
    public Object getExecutableReturnValue() {
        return null;
    }

    @Override
    public Path getPropertyPath() {
        return propertyPath;
    }

    @Override
    public Object getInvalidValue() {
        return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
    }

    @Override
    public Object unwrap(Class type) {
        return null;
    }

}


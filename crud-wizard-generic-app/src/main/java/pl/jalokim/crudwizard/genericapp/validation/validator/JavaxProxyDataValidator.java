package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator.formatPlaceholderFor;
import static pl.jalokim.crudwizard.genericapp.validation.validator.inner.AnnotationInvocationHandler.createAnnotationInstance;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.newInstance;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public abstract class JavaxProxyDataValidator<A extends Annotation, T> extends BaseDataValidator<T> {

    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasValidValue(T value, ValidationSessionContext validationContext) {
        Class<?> javaxValidatorClass = getValidatorsByType().keySet()
            .stream()
            .filter(canValidateType -> isTypeOf(value, canValidateType))
            .map(getValidatorsByType()::get)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Not supported value : %s in class: %s", value, getFullClassName(value))));

        ConstraintValidator<A, T> javaxSizeValidator = (ConstraintValidator<A, T>) newInstance(javaxValidatorClass);

        A javaxAnnotation = createAnnotationInstance(getJavaxAnnotationType(), messagePlaceholderArgs(validationContext));

        javaxSizeValidator.initialize(javaxAnnotation);

        return javaxSizeValidator.isValid(value, null);
    }

    @SuppressWarnings("unchecked")
    public Class<A> getJavaxAnnotationType() {
        return (Class<A>) getTypeMetadataFromClass(getClass())
            .getTypeMetaDataForParentClass(JavaxProxyDataValidator.class)
            .getGenericTypes().get(0).getRawType();
    }

    @Override
    public List<Class<?>> getTypesToValidate() {
        return elements(getValidatorsByType().keySet()).asList();
    }

    @Override
    public String messagePlaceholder() {
        return formatPlaceholderFor(getJavaxAnnotationType());
    }

    public abstract Map<Class<?>, Class<?>> getValidatorsByType();

}

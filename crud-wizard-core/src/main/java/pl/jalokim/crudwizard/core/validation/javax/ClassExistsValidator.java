package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isConcreteClass;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;

public class ClassExistsValidator implements BaseConstraintValidatorWithDynamicMessage<ClassExists, String> {

    private Class<?> expectedClassType;
    private boolean canBeAbstractOrInterface;

    @Override
    public void initialize(ClassExists constraintAnnotation) {
        expectedClassType = constraintAnnotation.expectedOfType();
        canBeAbstractOrInterface = constraintAnnotation.canBeAbstractOrInterface();
    }

    @Override
    public boolean isValidValue(String className, ConstraintValidatorContext context) {
        try {
            addMessageParameter(context, "expectedOfType", expectedClassType.getCanonicalName());
            Class<?> realClass = ClassUtils.loadRealClass(className);

            return isTypeOf(realClass, expectedClassType)
                && (canBeAbstractOrInterface || isConcreteClass(realClass));
        } catch (Exception ex) {
            return false;
        }
    }
}

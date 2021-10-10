package pl.jalokim.crudwizard.genericapp.metamodel.validation.javax;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

public class ClassExistsValidator implements BaseConstraintValidatorWithDynamicMessage<ClassExists, String> {

    private Class<?> expectedClassType;

    @Override
    public void initialize(ClassExists constraintAnnotation) {
        expectedClassType = constraintAnnotation.typeOfClass();
    }

    @Override
    public boolean isValidValue(String className, ConstraintValidatorContext context) {
        try {
            addMessageParameter(context, "typeOfClass", expectedClassType.getCanonicalName());
            Class<?> realClass = ClassUtils.loadRealClass(className);
            return MetadataReflectionUtils.isTypeOf(realClass, expectedClassType);
        } catch (Exception ex) {
            return false;
        }
    }
}

package pl.jalokim.crudwizard.genericapp.metamodel.datastorage.validation;

import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageInstances;

@RequiredArgsConstructor
@Component
public class VerifyThatCanCreateDataStorageValidator
    implements BaseConstraintValidatorWithDynamicMessage<VerifyThatCanCreateDataStorage, String> {

    private final DataStorageInstances dataStorageInstances;

    @Override
    public boolean isValidValue(String className, ConstraintValidatorContext context) {
        return dataStorageInstances.getDataStorageFactoryForClass(ClassUtils.loadRealClass(className)) != null;
    }
}

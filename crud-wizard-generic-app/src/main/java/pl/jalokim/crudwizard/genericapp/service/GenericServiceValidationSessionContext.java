package pl.jalokim.crudwizard.genericapp.service;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.crudwizard.genericapp.validation.ValidatorModelContext;

public class GenericServiceValidationSessionContext extends ValidationSessionContext {

    public GenericServiceValidationSessionContext() {
        setCurrentValidatorContext(new ValidatorModelContext(ValidatorMetaModel.builder().build(),
            null, PropertyPath.createRoot()));
    }

    @Override
    public void throwExceptionWhenErrorsOccurred() {
        getCurrentValidatorContext().getCurrentEntries().forEach(getValidationResult()::addEntry);
        super.throwExceptionWhenErrorsOccurred();
    }
}

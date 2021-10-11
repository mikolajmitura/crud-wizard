package pl.jalokim.crudwizard.genericapp.service;

import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.crudwizard.genericapp.validation.ValidatorModelContext;

public class GenericServiceValidationSessionContext extends ValidationSessionContext {

    public GenericServiceValidationSessionContext() {
        setCurrentValidatorContext(new ValidatorModelContext(new ValidatorMetaModel(), null, PropertyPath.createRoot()));
    }

    @Override
    public void throwExceptionWhenErrorsOccurred() {
        getCurrentValidatorContext().getCurrentEntries().forEach(getValidationResult()::addEntry);
        super.throwExceptionWhenErrorsOccurred();
    }
}

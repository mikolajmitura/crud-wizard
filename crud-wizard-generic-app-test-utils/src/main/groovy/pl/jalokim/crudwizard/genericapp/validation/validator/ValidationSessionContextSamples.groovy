package pl.jalokim.crudwizard.genericapp.validation.validator

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.metamodels.PropertyPath
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModelSamples
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext

class ValidationSessionContextSamples {

    static ValidationSessionContext createValidationSessionContext(DataValidator<?> dataValidatorInstance, Object nullableInstance,
        Map<String, Object> messagePlaceholders = [:], PropertyPath propertyPath = PropertyPath.createRoot()) {
        def validatorMetaModel = ValidatorMetaModelSamples.createValidatorMetaModel(dataValidatorInstance, messagePlaceholders)
        def validationSessionContext = new ValidationSessionContext()

        ClassMetaModel classMetaModel = Optional.ofNullable(nullableInstance)
            .map({instance -> instance.getClass()})
            .map({type -> createClassMetaModelFromClass(type)})
            .orElse(null)

        validationSessionContext.newValidatorContext(validatorMetaModel, classMetaModel, propertyPath)
        validationSessionContext
    }

    static ValidationSessionContext createValidationSessionContext(DataValidator<?> dataValidatorInstance, ClassMetaModel validatedClassMetaModel,
        Map<String, Object> messagePlaceholders = [:], PropertyPath propertyPath = PropertyPath.createRoot()) {
        def validatorMetaModel = ValidatorMetaModelSamples.createValidatorMetaModel(dataValidatorInstance, messagePlaceholders)
        def validationSessionContext = new ValidationSessionContext()
        validationSessionContext.newValidatorContext(validatorMetaModel, validatedClassMetaModel, propertyPath)
        validationSessionContext
    }
}

package pl.jalokim.crudwizard.core.metamodels

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel.PLACEHOLDER_PREFIX

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel
import pl.jalokim.crudwizard.genericapp.validation.validator.ClassMetaModelWithParentsValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SomeOtherObjectValidator

class ValidatorMetaModelSamples {

    public static final NOT_NULL_VALIDATOR = new NotNullValidator()
    public static final SIZE_VALIDATOR = new SizeValidator()
    public static final CUSTOM_TEST_VALIDATOR = new ClassMetaModelWithParentsValidator()
    public static final SOME_OTHER_OBJECT_VALIDATOR = new SomeOtherObjectValidator()
    public static final NOT_NULL_VALIDATOR_METAMODEL = createValidatorMetaModel(NOT_NULL_VALIDATOR)
    public static final SIZE_2_30_VALIDATOR_METAMODEL = createValidatorMetaModel(SIZE_VALIDATOR, [
        min: 2,
        max: 30
    ])
    public static final SIZE_3_20_VALIDATOR_METAMODEL = createValidatorMetaModel(SIZE_VALIDATOR, [
        min: 3,
        max: 20
    ])
    public static final SIZE_1_MAX_VALIDATOR_METAMODEL = createValidatorMetaModel(SIZE_VALIDATOR, [min: 1])
    public static final CUSTOM_TEST_VALIDATOR_METAMODEL = createValidatorMetaModel(CUSTOM_TEST_VALIDATOR)
    public static final SOME_OTHER_OBJECT_VALIDATOR_METAMODEL = createValidatorMetaModel(SOME_OTHER_OBJECT_VALIDATOR)

    static ValidatorMetaModel createValidatorMetaModel(DataValidator<?> dataValidatorInstance, Map<String, Object> messagePlaceholders = [:]) {
        def validatorMetaModel = ValidatorMetaModel.builder()
            .realClass(dataValidatorInstance.getClass())
            .validatorInstance(dataValidatorInstance)
            .namePlaceholder(dataValidatorInstance.namePlaceholder())
            .messagePlaceholder(dataValidatorInstance.messagePlaceholder())
            .build()

        messagePlaceholders.forEach({name, value ->
            validatorMetaModel.addProperty(PLACEHOLDER_PREFIX + name, value)
        })

        validatorMetaModel
    }
}

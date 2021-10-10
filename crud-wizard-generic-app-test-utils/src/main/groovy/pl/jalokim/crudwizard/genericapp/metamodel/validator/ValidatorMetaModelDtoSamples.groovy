package pl.jalokim.crudwizard.genericapp.metamodel.validator

import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator

class ValidatorMetaModelDtoSamples {

    static ValidatorMetaModelDto createValidValidatorMetaModelDto(Class<? extends DataValidator> validatorInstanceType,
        String validatorName, Map<String, Object> additionalProperties = [:]) {
        def validatorMetamodel = ValidatorMetaModelDto.builder()
            .className(validatorInstanceType.canonicalName)
            .validatorName(validatorName)
            .parametrized(!additionalProperties.isEmpty())
            .build()

        additionalProperties.forEach {
            name, value ->
                validatorMetamodel
                    .addProperty(ValidatorMetaModel.PLACEHOLDER_PREFIX + name, value)
        }

        validatorMetamodel
    }

    static ValidatorMetaModelDto createValidValidatorMetaModelDto() {
        ValidatorMetaModelDto.builder()
            .className(DataValidator.canonicalName)
            .build()
    }

    static ValidatorMetaModelDto createEmptyValidatorMetaModelDto() {
        ValidatorMetaModelDto.builder()
            .build()
    }
}

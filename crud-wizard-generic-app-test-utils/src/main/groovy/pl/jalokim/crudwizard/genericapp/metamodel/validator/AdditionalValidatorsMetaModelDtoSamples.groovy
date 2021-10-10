package pl.jalokim.crudwizard.genericapp.metamodel.validator

import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto

import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator

class AdditionalValidatorsMetaModelDtoSamples {

    static List<AdditionalValidatorsMetaModelDto> createAdditionalValidatorsForExtendedPerson() {
        [
            AdditionalValidatorsMetaModelDto.builder()
                .fullPropertyPath("name")
                .validators([
                    createValidValidatorMetaModelDto(NotNullValidator, NotNullValidator.NOT_NULL),
                    createValidValidatorMetaModelDto(SizeValidator, SizeValidator.VALIDATOR_KEY_SIZE, [min: 2, max: 20])
                ])
                .build(),
            AdditionalValidatorsMetaModelDto.builder()
                .fullPropertyPath("surname")
                .validators([
                    createValidValidatorMetaModelDto(NotNullValidator, NotNullValidator.NOT_NULL),
                    createValidValidatorMetaModelDto(SizeValidator, SizeValidator.VALIDATOR_KEY_SIZE, [min: 2, max: 30])
                ])
                .build(),
            AdditionalValidatorsMetaModelDto.builder()
                .fullPropertyPath("documents")
                .validators([createValidValidatorMetaModelDto(SizeValidator, SizeValidator.VALIDATOR_KEY_SIZE, [min: 1])])
                .build(),
            AdditionalValidatorsMetaModelDto.builder()
                .fullPropertyPath("documents[*].type")
                .validators([createValidValidatorMetaModelDto(NotNullValidator, NotNullValidator.NOT_NULL)])
                .build()
        ]
    }
}

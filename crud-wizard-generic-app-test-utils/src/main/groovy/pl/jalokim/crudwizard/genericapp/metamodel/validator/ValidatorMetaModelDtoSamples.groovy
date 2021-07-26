package pl.jalokim.crudwizard.genericapp.metamodel.validator

import static pl.jalokim.utils.test.DataFakerHelper.randomText

class ValidatorMetaModelDtoSamples {

    static ValidatorMetaModelDto createValidValidatorMetaModelDto() {
        ValidatorMetaModelDto.builder()
            .className(randomText())
            .build()
    }

    static ValidatorMetaModelDto createEmptyValidatorMetaModelDto() {
        ValidatorMetaModelDto.builder()
            .build()
    }
}

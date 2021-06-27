package pl.jalokim.crudwizard.genericapp.metamodel.validator

import static pl.jalokim.crudwizard.test.utils.random.DataFakerHelper.randomText

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

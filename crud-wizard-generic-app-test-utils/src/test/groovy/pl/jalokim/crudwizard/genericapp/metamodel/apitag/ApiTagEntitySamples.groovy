package pl.jalokim.crudwizard.genericapp.metamodel.apitag

import pl.jalokim.utils.test.DataFakerHelper

class ApiTagEntitySamples {

    static ApiTagEntity sampleApiTagEntity() {
        ApiTagEntity.builder()
            .name(DataFakerHelper.randomText())
            .build()
    }
}

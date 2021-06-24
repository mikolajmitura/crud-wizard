package pl.jalokim.crudwizard.genericapp.metamodel.apitag

import pl.jalokim.crudwizard.test.utils.random.DataFakerHelper

class ApiTagEntitySamples {

    static ApiTagEntity sampleApiTagEntity() {
        ApiTagEntity.builder()
            .name(DataFakerHelper.randomText())
            .build()
    }
}

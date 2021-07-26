package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelTransactional
import pl.jalokim.utils.test.DataFakerHelper;

@Component
@MetamodelTransactional
class ApiTagSamples {

    @Autowired
    private ApiTagRepository apiTagRepository

    ApiTagEntity saveNewApiTag() {
        saveNewApiTag(sampleApiTagEntity())
    }

    ApiTagEntity saveNewApiTag(ApiTagEntity apiTagEntity) {
        apiTagRepository.save(apiTagEntity)
    }

    static ApiTagEntity sampleApiTagEntity() {
        ApiTagEntity.builder()
            .name(DataFakerHelper.randomText())
            .build()
    }
}

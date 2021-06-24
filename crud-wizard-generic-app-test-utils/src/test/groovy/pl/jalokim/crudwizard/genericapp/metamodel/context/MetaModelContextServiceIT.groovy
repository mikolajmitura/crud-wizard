package pl.jalokim.crudwizard.genericapp.metamodel.context

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagRepository

class MetaModelContextServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private ApiTagRepository apiTagRepository

    // TODO #3 to implement load context
    // TODO #4 to implement load context with urls hierarchy etc
//    def "should load all meta models as expected"() {
//        given:
//        def firstApiTag = apiTagRepository.save(sampleApiTagEntity())
//        def secondApiTag = apiTagRepository.save(sampleApiTagEntity())
//
//        when:
//
//        then:
//
//    }


}
